package io.harness.mongo;

import static io.harness.govern.Switch.unhandled;
import static io.harness.persistence.HQuery.excludeAuthority;
import static java.lang.String.format;

import com.google.inject.Inject;

import io.harness.persistence.HPersistence;
import io.harness.persistence.ReadPref;
import io.harness.queue.Queuable;
import io.harness.queue.Queue;
import io.harness.version.VersionInfoManager;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MongoQueue<T extends Queuable> implements Queue<T> {
  private static final Logger logger = LoggerFactory.getLogger(MongoQueue.class);

  private final Class<T> klass;
  private int resetDurationInSeconds;
  private final boolean filterWithVersion;

  private Semaphore semaphore = new Semaphore(1);
  @Inject private HPersistence persistence;
  @Inject private VersionInfoManager versionInfoManager;

  /**
   * Instantiates a new mongo queue impl.
   *
   * @param klass     the klass
   * @param datastore the datastore
   */
  public MongoQueue(Class<T> klass) {
    this(klass, 5);
  }

  /**
   * Instantiates a new mongo queue impl.
   *
   * @param klass                  the klass
   * @param resetDurationInSeconds the reset duration in seconds
   */
  public MongoQueue(Class<T> klass, int resetDurationInSeconds) {
    this(klass, resetDurationInSeconds, false);
  }

  /**
   * Instantiates a new mongo queue impl.
   *
   * @param klass                  the klass
   * @param resetDurationInSeconds the reset duration in seconds
   * @param filterWithVersion      the filterWithVersion
   */
  public MongoQueue(Class<T> klass, int resetDurationInSeconds, boolean filterWithVersion) {
    Objects.requireNonNull(klass);
    this.klass = klass;
    this.resetDurationInSeconds = resetDurationInSeconds;
    this.filterWithVersion = filterWithVersion;
  }

  @Override
  public T get(Duration wait, Duration poll) {
    long endTime = System.currentTimeMillis() + wait.toMillis();

    boolean acquired = false;
    try {
      if (acquired = semaphore.tryAcquire(wait.toMillis(), TimeUnit.MILLISECONDS)) {
        return getUnderLock(endTime, poll);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      if (acquired) {
        semaphore.release();
      }
    }
    return null;
  }

  private T getUnderLock(long endTime, Duration poll) {
    final AdvancedDatastore datastore = persistence.getDatastore(klass, ReadPref.CRITICAL);

    while (true) {
      final Date now = new Date();

      Query<T> query = createQuery();
      query.or(query.criteria(Queuable.RUNNING_KEY).equal(false),
          query.criteria(Queuable.RESET_TIMESTAMP_KEY).lessThanOrEq(now));
      query.field(Queuable.EARLIEST_GET_KEY)
          .lessThanOrEq(now)
          .order(Sort.descending(Queuable.PRIORITY_KEY), Sort.ascending(Queuable.CREATED_KEY));

      UpdateOperations<T> updateOperations =
          datastore.createUpdateOperations(klass)
              .set(Queuable.RUNNING_KEY, true)
              .set(Queuable.RESET_TIMESTAMP_KEY, new Date(now.getTime() + resetDurationMillis()));

      T message = HPersistence.retry(() -> datastore.findAndModify(query, updateOperations));
      if (message != null) {
        return message;
      }

      if (System.currentTimeMillis() >= endTime) {
        return null;
      }

      try {
        Thread.sleep(poll.toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      } catch (final IllegalArgumentException ex) {
        poll = Duration.ofMillis(0);
      }
    }
  }

  @Override
  public void updateResetDuration(T message) {
    Date resetTimestamp = new Date(System.currentTimeMillis() + resetDurationMillis());

    Query<T> query = persistence.createQuery(klass)
                         .filter(Queuable.ID_KEY, message.getId())
                         .field(Queuable.RESET_TIMESTAMP_KEY)
                         .lessThan(resetTimestamp)
                         .filter(Queuable.RUNNING_KEY, true);

    UpdateOperations<T> updateOperations =
        persistence.createUpdateOperations(klass).set(Queuable.RESET_TIMESTAMP_KEY, resetTimestamp);

    if (persistence.findAndModify(query, updateOperations, HPersistence.returnOldOptions) != null) {
      message.setResetTimestamp(resetTimestamp);
      return;
    }
    final T currentState = persistence.createQuery(klass).filter(Queuable.ID_KEY, message.getId()).get();
    logger.error("Reset duration failed for {}", currentState.toString());
  }

  @Override
  // This API is used only for testing, we do not need index for the running field. If you start using the
  // API in production, please consider adding such.
  public long count(final Filter filter) {
    final AdvancedDatastore datastore = persistence.getDatastore(klass, ReadPref.CRITICAL);

    switch (filter) {
      case ALL:
        return datastore.getCount(klass);
      case RUNNING:
        return datastore.getCount(createQuery().filter(Queuable.RUNNING_KEY, true));
      case NOT_RUNNING:
        return datastore.getCount(createQuery().filter(Queuable.RUNNING_KEY, false));
      default:
        unhandled(filter);
    }
    throw new RuntimeException(format("Unknown filter type %s", filter));
  }

  @Override
  public void ack(final T message) {
    Objects.requireNonNull(message);
    persistence.delete(klass, message.getId());
  }

  @Override
  public void requeue(final String id, int retries) {
    requeue(id, retries, new Date());
  }

  @Override
  public void requeue(final String id, final int retries, final Date earliestGet) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(earliestGet);

    persistence.update(persistence.createQuery(klass, excludeAuthority).filter(Queuable.ID_KEY, id),
        persistence.createUpdateOperations(klass)
            .set(Queuable.RUNNING_KEY, false)
            .set(Queuable.RETRIES_KEY, retries)
            .set(Queuable.EARLIEST_GET_KEY, earliestGet));
  }

  @Override
  public void send(final T payload) {
    Objects.requireNonNull(payload);
    payload.setVersion(versionInfoManager.getVersionInfo().getVersion());

    final AdvancedDatastore datastore = persistence.getDatastore(klass, ReadPref.CRITICAL);
    HPersistence.retry(() -> datastore.save(payload));
  }

  @Override
  public long resetDurationMillis() {
    return TimeUnit.SECONDS.toMillis(resetDurationInSeconds);
  }

  @Override
  public String name() {
    return persistence.getCollection(klass, ReadPref.CRITICAL).getName();
  }

  /**
   * Reset duration.
   *
   * @param resetDurationInSeconds the reset duration in seconds
   */
  // package protected
  public void resetDuration(int resetDurationInSeconds) {
    this.resetDurationInSeconds = resetDurationInSeconds;
  }

  private Query<T> createQuery() {
    final Query<T> query = persistence.createQuery(klass, ReadPref.CRITICAL);
    if (filterWithVersion) {
      query.filter(Queuable.VERSION_KEY, versionInfoManager.getVersionInfo().getVersion());
    }
    return query;
  }
}
