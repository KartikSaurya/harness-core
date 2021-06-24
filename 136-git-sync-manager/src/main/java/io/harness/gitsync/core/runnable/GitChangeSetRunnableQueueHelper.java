package io.harness.gitsync.core.runnable;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.gitsync.common.beans.YamlChangeSet;
import io.harness.gitsync.common.beans.YamlChangeSetEventType;
import io.harness.gitsync.common.beans.YamlChangeSetStatus;
import io.harness.gitsync.core.dtos.YamlChangeSetDTO;
import io.harness.gitsync.core.service.YamlChangeSetService;
import io.harness.lock.AcquiredLock;
import io.harness.lock.PersistentLocker;

import com.google.inject.Singleton;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@OwnedBy(DX)
public class GitChangeSetRunnableQueueHelper {
  private static final AtomicLong lastTimestampForChangesetLogPrint = new AtomicLong(0);

  public YamlChangeSetDTO getQueuedChangeSetForWaitingQueueKey(String accountId, String queueKey,
      int maxRunningChangesetsForAccount, YamlChangeSetService ycsService, List<YamlChangeSetStatus> runningStatusList,
      PersistentLocker persistentLocker) {
    if (accountQuotaMaxedOut(accountId, maxRunningChangesetsForAccount, ycsService, runningStatusList)) {
      log.info("Account quota has been reached. Returning null");
      return null;
    }

    return selectQueuedChangeSetWithPriority(accountId, queueKey, ycsService, persistentLocker, runningStatusList);
  }

  private boolean accountQuotaMaxedOut(String accountId, int maxRunningChangesetsForAccount,
      YamlChangeSetService yamlChangeSetService, List<YamlChangeSetStatus> runningStatus) {
    return yamlChangeSetService.countByAccountIdAndStatus(accountId, runningStatus) >= maxRunningChangesetsForAccount;
  }

  private YamlChangeSetDTO selectQueuedChangeSetWithPriority(String accountId, String queueKey,
      YamlChangeSetService ycsService, PersistentLocker persistentLocker, List<YamlChangeSetStatus> runningStatusList) {
    /**
     * Priority of items in queue -
     * 1. BRANCH_CREATE
     * 2. BRANCH_SYNC
     * 3. G2H
     */

    try (AcquiredLock lock = persistentLocker.waitToAcquireLock(
             YamlChangeSet.class, accountId, Duration.ofMinutes(2), Duration.ofSeconds(10))) {
      YamlChangeSetDTO selectedYamlChangeSet = null;

      // Some other instance already picked up and marked status as running skip in that case.
      if (ycsService.changeSetExistsFoQueueKey(accountId, queueKey, runningStatusList)) {
        log.info("Found running changeset for queuekey. Returning null");
        return null;
      }

      Optional<YamlChangeSetDTO> changeSet = ycsService.peekQueueHead(accountId, queueKey, YamlChangeSetStatus.QUEUED);
      if (changeSet.isPresent()) {
        selectedYamlChangeSet = changeSet.get();
      }

      // Not using comparator currently, can be added later.
      //      final List<YamlChangeSet> sortedChangeSets =
      //          changeSets.stream().sorted(new YamlChangeSetComparator()).collect(Collectors.toList());
      //      if (isNotEmpty(sortedChangeSets)) {
      //        selectedYamlChangeSet = sortedChangeSets.get(0);
      //      }

      if (selectedYamlChangeSet != null) {
        if (!isChangeSetEligibleForCurrentRun(selectedYamlChangeSet)) {
          return null;
        }
        final boolean updateStatus = ycsService.updateStatusAndCutoffTime(
            accountId, selectedYamlChangeSet.getChangesetId(), YamlChangeSetStatus.RUNNING);
        if (updateStatus) {
          return ycsService.get(accountId, selectedYamlChangeSet.getChangesetId()).orElse(null);
        } else {
          log.error("error while updating status of yaml change set Id = [{}]. Skipping selection",
              selectedYamlChangeSet.getChangesetId());
        }
      }
      return null;
    }
  }

  private boolean isChangeSetEligibleForCurrentRun(YamlChangeSetDTO selectedYamlChangeSet) {
    final long currentTimeMillis = System.currentTimeMillis();
    if (selectedYamlChangeSet.getNextRunTime() > currentTimeMillis) {
      // todo(abhinav): have better logging logic
      // logging on random logic
      if (currentTimeMillis % 50 == 0) {
        log.info("Skipping changeset: [{}] since its next run time: [{}] is still ahead of current time: [{}]",
            selectedYamlChangeSet.getChangesetId(), selectedYamlChangeSet.getNextRunTime(), currentTimeMillis);
      }
      return false;
    }
    return true;
  }

  public class YamlChangeSetComparator implements Comparator<YamlChangeSet> {
    Map<String, Integer> statusOrder = new HashMap<String, Integer>() {
      {
        put(YamlChangeSetEventType.BRANCH_CREATE.name(), 1);
        put(YamlChangeSetEventType.BRANCH_SYNC.name(), 2);
        put(YamlChangeSetEventType.BRANCH_PUSH.name(), 3);
      }
    };

    @Override
    public int compare(YamlChangeSet o1, YamlChangeSet o2) {
      final YamlChangeSetEventType eventType1 = o1.getEventType();
      final YamlChangeSetEventType eventType2 = o2.getEventType();

      return statusOrder.get(eventType1) - statusOrder.get(eventType2);
    }
  }
}
