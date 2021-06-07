package io.harness.analyserservice;

import static io.harness.AuthorizationServiceHeader.ANALYZER_SERVICE;
import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.event.QueryAnalysisMessageListener;
import io.harness.eventsframework.EventsFrameworkConfiguration;
import io.harness.eventsframework.EventsFrameworkConstants;
import io.harness.eventsframework.api.Consumer;
import io.harness.eventsframework.impl.noop.NoOpConsumer;
import io.harness.eventsframework.impl.redis.RedisConsumer;
import io.harness.ng.core.event.MessageListener;
import io.harness.redis.RedisConfig;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@OwnedBy(PIPELINE)
public class EventsFrameworkModule extends AbstractModule {
  private final EventsFrameworkConfiguration eventsFrameworkConfiguration;

  @Override
  protected void configure() {
    RedisConfig redisConfig = this.eventsFrameworkConfiguration.getRedisConfig();
    if (redisConfig.getRedisUrl().equals("dummyRedisUrl")) {
      bind(Consumer.class)
          .annotatedWith(Names.named(EventsFrameworkConstants.ANALYZER_SERVICE))
          .toInstance(
              NoOpConsumer.of(EventsFrameworkConstants.DUMMY_TOPIC_NAME, EventsFrameworkConstants.DUMMY_GROUP_NAME));
    } else {
      bind(Consumer.class)
          .annotatedWith(Names.named(EventsFrameworkConstants.ANALYZER_SERVICE))
          .toInstance(RedisConsumer.of(EventsFrameworkConstants.ANALYZER_SERVICE, ANALYZER_SERVICE.getServiceId(),
              redisConfig, EventsFrameworkConstants.ENTITY_CRUD_MAX_PROCESSING_TIME,
              EventsFrameworkConstants.ENTITY_CRUD_READ_BATCH_SIZE));
    }
    bind(MessageListener.class)
        .annotatedWith(Names.named(EventsFrameworkConstants.ANALYZER_SERVICE))
        .to(QueryAnalysisMessageListener.class);
  }
}