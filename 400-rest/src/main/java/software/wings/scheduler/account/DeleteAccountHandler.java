/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.scheduler.account;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.mongo.iterator.MongoPersistenceIterator.SchedulingType.REGULAR;

import static software.wings.utils.TimeUtils.isWeekend;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

import io.harness.annotations.dev.OwnedBy;
import io.harness.iterator.PersistenceIteratorFactory;
import io.harness.mongo.iterator.MongoPersistenceIterator;
import io.harness.mongo.iterator.MongoPersistenceIterator.Handler;
import io.harness.mongo.iterator.filter.MorphiaFilterExpander;
import io.harness.mongo.iterator.provider.MorphiaPersistenceProvider;

import software.wings.app.JobsFrequencyConfig;
import software.wings.beans.Account;
import software.wings.beans.Account.AccountKeys;
import software.wings.beans.AccountStatus;
import software.wings.service.intfc.AccountService;

import com.google.inject.Inject;

@OwnedBy(PL)
public class DeleteAccountHandler implements Handler<Account> {
  @Inject private PersistenceIteratorFactory persistenceIteratorFactory;
  @Inject private AccountService accountService;
  @Inject private JobsFrequencyConfig jobsFrequencyConfig;
  @Inject private MorphiaPersistenceProvider<Account> persistenceProvider;

  public void registerIterators() {
    persistenceIteratorFactory.createPumpIteratorWithDedicatedThreadPool(
        PersistenceIteratorFactory.PumpExecutorOptions.builder()
            .name("DeleteAccountIterator")
            .poolSize(2)
            .interval(ofMinutes(1))
            .build(),
        DeleteAccountHandler.class,
        MongoPersistenceIterator.<Account, MorphiaFilterExpander<Account>>builder()
            .clazz(Account.class)
            .fieldName(AccountKeys.accountDeletionIteration)
            .targetInterval(ofMinutes(jobsFrequencyConfig.getAccountDeletionJobFrequencyInMinutes()))
            .acceptableNoAlertDelay(ofMinutes(Integer.MAX_VALUE))
            .acceptableExecutionTime(ofSeconds(120))
            .persistenceProvider(persistenceProvider)
            .handler(this)
            .schedulingType(REGULAR)
            .redistribute(true));
  }

  @Override
  public void handle(Account account) {
    // Delete accounts only on weekend
    if (isWeekend() && AccountStatus.MARKED_FOR_DELETION.equals(account.getLicenseInfo().getAccountStatus())) {
      accountService.delete(account.getUuid());
    }
  }
}
