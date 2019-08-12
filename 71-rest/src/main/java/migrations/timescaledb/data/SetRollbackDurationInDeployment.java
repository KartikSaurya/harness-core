package migrations.timescaledb.data;

import static io.harness.persistence.HQuery.excludeAuthority;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.mongodb.ReadPreference;
import io.harness.beans.ExecutionStatus;
import io.harness.persistence.HIterator;
import io.harness.timescaledb.TimeScaleDBService;
import lombok.extern.slf4j.Slf4j;
import migrations.TimeScaleDBDataMigration;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Sort;
import software.wings.beans.WorkflowExecution;
import software.wings.beans.WorkflowExecution.WorkflowExecutionKeys;
import software.wings.dl.WingsPersistence;
import software.wings.service.impl.WorkflowExecutionUpdate;
import software.wings.sm.StateExecutionInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This migration will set rollback duration for the last 60 days of top level executions to TimeScaleDB
 * @author rktummala
 */
@Slf4j
@Singleton
public class SetRollbackDurationInDeployment implements TimeScaleDBDataMigration {
  @Inject TimeScaleDBService timeScaleDBService;

  @Inject WingsPersistence wingsPersistence;
  @Inject WorkflowExecutionUpdate workflowExecutionUpdate;

  private static final int MAX_RETRY = 5;

  private static final String update_statement = "UPDATE DEPLOYMENT SET ROLLBACK_DURATION=? WHERE EXECUTIONID=?";

  private static final String query_statement = "SELECT * FROM DEPLOYMENT WHERE EXECUTIONID=?";

  @Override
  public boolean migrate() {
    if (!timeScaleDBService.isValid()) {
      logger.info("TimeScaleDB not found, not migrating deployment data to TimeScaleDB");
      return false;
    }
    int count = 0;
    try {
      FindOptions findOptions = new FindOptions();
      findOptions.readPreference(ReadPreference.secondaryPreferred());
      try (HIterator<WorkflowExecution> iterator =
               new HIterator<>(wingsPersistence.createQuery(WorkflowExecution.class, excludeAuthority)
                                   .field(WorkflowExecutionKeys.createdAt)
                                   .greaterThanOrEq(System.currentTimeMillis() - (60 * 24 * 3600 * 1000L))
                                   .field(WorkflowExecutionKeys.pipelineExecutionId)
                                   .doesNotExist()
                                   .field(WorkflowExecutionKeys.startTs)
                                   .exists()
                                   .field(WorkflowExecutionKeys.endTs)
                                   .exists()
                                   .field(WorkflowExecutionKeys.status)
                                   .in(ExecutionStatus.finalStatuses())
                                   .field(WorkflowExecutionKeys.accountId)
                                   .exists()
                                   .order(Sort.descending(WorkflowExecutionKeys.createdAt))
                                   .fetch(findOptions))) {
        while (iterator.hasNext()) {
          WorkflowExecution workflowExecution = iterator.next();
          updateWorkflowExecutionWithRollbackDuration(workflowExecution);
          count++;
          if (count % 100 == 0) {
            logger.info("Completed migrating workflow execution [{}] records", count);
          }
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to complete rollback duration migration", e);
      return false;
    } finally {
      logger.info("Completed updating [{}] records", count);
    }
    return true;
  }

  private void updateWorkflowExecutionWithRollbackDuration(WorkflowExecution workflowExecution) {
    long startTime = System.currentTimeMillis();
    boolean successful = false;
    int retryCount = 0;

    final StateExecutionInstance rollbackInstance =
        workflowExecutionUpdate.getRollbackInstance(workflowExecution.getUuid());
    Long rollbackStartTs = rollbackInstance == null ? null : rollbackInstance.getStartTs();
    Long rollbackDuration = rollbackStartTs == null ? null : workflowExecution.getEndTs() - rollbackStartTs;

    if (rollbackDuration == null) {
      rollbackDuration = 0L;
    }

    while (!successful && retryCount < MAX_RETRY) {
      try (Connection connection = timeScaleDBService.getDBConnection();
           PreparedStatement queryStatement = connection.prepareStatement(query_statement);
           PreparedStatement updateStatement = connection.prepareStatement(update_statement)) {
        queryStatement.setString(1, workflowExecution.getUuid());
        ResultSet queryResult = queryStatement.executeQuery();
        if (queryResult != null && queryResult.next()) {
          logger.info("WorkflowExecution found:[{}],updating it", workflowExecution.getUuid());
          updateDataInTimescaleDB(workflowExecution, updateStatement, rollbackDuration);
        }

        successful = true;
      } catch (SQLException e) {
        if (retryCount >= MAX_RETRY) {
          logger.error("Failed to update workflowExecution,[{}]", workflowExecution.getUuid(), e);
        } else {
          logger.info(
              "Failed to update workflowExecution,[{}],retryCount=[{}]", workflowExecution.getUuid(), retryCount);
        }
        retryCount++;
      } catch (Exception e) {
        logger.error("Failed to update workflowExecution,[{}]", workflowExecution.getUuid(), e);
        retryCount = MAX_RETRY + 1;
      } finally {
        logger.info("Total update time =[{}] for workflowExecution:[{}]", System.currentTimeMillis() - startTime,
            workflowExecution.getUuid());
      }
    }
  }

  private void updateDataInTimescaleDB(WorkflowExecution workflowExecution, PreparedStatement updateStatement,
      long rollbackDuration) throws SQLException {
    updateStatement.setLong(1, rollbackDuration);
    updateStatement.setString(2, workflowExecution.getUuid());
    updateStatement.execute();
  }
}
