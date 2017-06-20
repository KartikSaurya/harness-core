package software.wings.service.impl.splunk;

import software.wings.beans.SplunkConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rsingh on 5/18/17.
 */
public class SplunkDataCollectionInfo {
  private String accountId;
  private String applicationId;
  private SplunkConfig splunkConfig;
  private List<String> queries = new ArrayList<>();
  private int collectionTime;

  public SplunkDataCollectionInfo() {}

  public SplunkDataCollectionInfo(
      String accountId, String applicationId, SplunkConfig splunkConfig, List<String> queries, int collectionTime) {
    this.accountId = accountId;
    this.applicationId = applicationId;
    this.splunkConfig = splunkConfig;
    this.queries = queries;
    this.collectionTime = collectionTime;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public SplunkConfig getSplunkConfig() {
    return splunkConfig;
  }

  public void setSplunkConfig(SplunkConfig splunkConfig) {
    this.splunkConfig = splunkConfig;
  }

  public List<String> getQueries() {
    return queries;
  }

  public void setQueries(List<String> queries) {
    this.queries = queries;
  }

  public int getCollectionTime() {
    return collectionTime;
  }

  public void setCollectionTime(int collectionTime) {
    this.collectionTime = collectionTime;
  }

  @Override
  public String toString() {
    return "SplunkDataCollectionInfo{"
        + "accountId='" + accountId + '\'' + ", applicationId='" + applicationId + '\''
        + ", splunkConfig=" + splunkConfig + ", queries=" + queries + ", collectionTime=" + collectionTime + '}';
  }
}
