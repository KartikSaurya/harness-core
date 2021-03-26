package io.harness.outbox;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.mongo.MongoConfig;
import io.harness.outbox.api.OutboxDao;
import io.harness.outbox.api.OutboxService;
import io.harness.outbox.api.impl.OutboxDaoImpl;
import io.harness.outbox.api.impl.OutboxServiceImpl;
import io.harness.persistence.HPersistence;
import io.harness.springdata.HTransactionTemplate;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@OwnedBy(PL)
@Slf4j
public class TransactionOutboxModule extends AbstractModule {
  public static final String OUTBOX_TRANSACTION_TEMPLATE = "OUTBOX_TRANSACTION_TEMPLATE";

  @Override
  protected void configure() {
    bind(OutboxDao.class).to(OutboxDaoImpl.class);
    bind(OutboxService.class).to(OutboxServiceImpl.class);
    registerRequiredBindings();
  }

  @Provides
  @Singleton
  @Named(OUTBOX_TRANSACTION_TEMPLATE)
  protected TransactionTemplate getTransactionTemplate(
      MongoTransactionManager mongoTransactionManager, MongoConfig mongoConfig) {
    return new HTransactionTemplate(mongoTransactionManager, mongoConfig.isTransactionsEnabled());
  }

  private void registerRequiredBindings() {
    requireBinding(HPersistence.class);
  }
}
