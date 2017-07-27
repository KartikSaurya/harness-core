package software.wings.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peeyushaggarwal on 11/28/16.
 */
@Entity(value = "delegates", noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Delegate extends Base {
  @NotEmpty private String accountId;
  private Status status = Status.ENABLED;
  private boolean connected;
  private String ip;
  private String hostName;
  private long lastHeartBeat;
  private String version;
  private List<TaskType> supportedTaskTypes;

  @Transient private List<DelegateTask> currentlyExecutingDelegateTasks;

  private List<DelegateScope> includeScopes = new ArrayList<>();
  private List<DelegateScope> excludeScopes = new ArrayList<>();

  public List<DelegateScope> getIncludeScopes() {
    return includeScopes;
  }

  public void setIncludeScopes(List<DelegateScope> includeScopes) {
    this.includeScopes = includeScopes;
  }

  public List<DelegateScope> getExcludeScopes() {
    return excludeScopes;
  }

  public void setExcludeScopes(List<DelegateScope> excludeScopes) {
    this.excludeScopes = excludeScopes;
  }

  /**
   * Getter for property 'accountId'.
   *
   * @return Value for property 'accountId'.
   */
  public String getAccountId() {
    return accountId;
  }

  /**
   * Setter for property 'accountId'.
   *
   * @param accountId Value to set for property 'accountId'.
   */
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  /**
   * Getter for property 'status'.
   *
   * @return Value for property 'status'.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Setter for property 'status'.
   *
   * @param status Value to set for property 'status'.
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Getter for property 'ip'.
   *
   * @return Value for property 'ip'.
   */
  public String getIp() {
    return ip;
  }

  /**
   * Setter for property 'ip'.
   *
   * @param ip Value to set for property 'ip'.
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Getter for property 'hostName'.
   *
   * @return Value for property 'hostName'.
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * Setter for property 'hostName'.
   *
   * @param hostName Value to set for property 'hostName'.
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * Getter for property 'lastHeartBeat'.
   *
   * @return Value for property 'lastHeartBeat'.
   */
  public long getLastHeartBeat() {
    return lastHeartBeat;
  }

  /**
   * Setter for property 'lastHeartBeat'.
   *
   * @param lastHeartBeat Value to set for property 'lastHeartBeat'.
   */
  public void setLastHeartBeat(long lastHeartBeat) {
    this.lastHeartBeat = lastHeartBeat;
  }

  /**
   * Getter for property 'version'.
   *
   * @return Value for property 'version'.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Setter for property 'version'.
   *
   * @param version Value to set for property 'version'.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Getter for property 'connected'.
   *
   * @return Value for property 'connected'.
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Setter for property 'connected'.
   *
   * @param connected Value to set for property 'connected'.
   */
  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  /**
   * Getter for property 'supportedTaskTypes'.
   *
   * @return Value for property 'supportedTaskTypes'.
   */
  public List<TaskType> getSupportedTaskTypes() {
    return supportedTaskTypes;
  }

  /**
   * Getter for property 'currentlyExecutingDelegateTasks'.
   *
   * @return Value for property 'currentlyExecutingDelegateTasks'.
   */
  public List<DelegateTask> getCurrentlyExecutingDelegateTasks() {
    return currentlyExecutingDelegateTasks;
  }

  /**
   * Setter for property 'currentlyExecutingDelegateTasks'.
   *
   * @param currentlyExecutingDelegateTasks Value to set for property 'currentlyExecutingDelegateTasks'.
   */
  public void setCurrentlyExecutingDelegateTasks(List<DelegateTask> currentlyExecutingDelegateTasks) {
    this.currentlyExecutingDelegateTasks = currentlyExecutingDelegateTasks;
  }

  /**
   * Setter for property 'supportedTaskTypes'.
   *
   * @param supportedTaskTypes Value to set for property 'supportedTaskTypes'.
   */
  public void setSupportedTaskTypes(List<TaskType> supportedTaskTypes) {
    this.supportedTaskTypes = supportedTaskTypes;
  }

  public enum Status { ENABLED, DISABLED }

  public static final class Builder {
    private String accountId;
    private Status status = Status.ENABLED;
    private boolean connected;
    private String ip;
    private String hostName;
    private long lastHeartBeat;
    private String version;
    private List<TaskType> supportedTaskTypes;
    private List<DelegateScope> includeScopes;
    private List<DelegateScope> excludeScopes;
    private List<DelegateTask> currentlyExecutingDelegateTasks;
    private String uuid;
    private String appId;
    private EmbeddedUser createdBy;
    private long createdAt;
    private EmbeddedUser lastUpdatedBy;
    private long lastUpdatedAt;

    private Builder() {}

    public static Builder aDelegate() {
      return new Builder();
    }

    public Builder withAccountId(String accountId) {
      this.accountId = accountId;
      return this;
    }

    public Builder withStatus(Status status) {
      this.status = status;
      return this;
    }

    public Builder withConnected(boolean connected) {
      this.connected = connected;
      return this;
    }

    public Builder withIp(String ip) {
      this.ip = ip;
      return this;
    }

    public Builder withHostName(String hostName) {
      this.hostName = hostName;
      return this;
    }

    public Builder withLastHeartBeat(long lastHeartBeat) {
      this.lastHeartBeat = lastHeartBeat;
      return this;
    }

    public Builder withVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder withSupportedTaskTypes(List<TaskType> supportedTaskTypes) {
      this.supportedTaskTypes = supportedTaskTypes;
      return this;
    }

    public Builder withIncludeScopes(List<DelegateScope> includeScopes) {
      this.includeScopes = includeScopes;
      return this;
    }

    public Builder withExcludeScopes(List<DelegateScope> excludeScopes) {
      this.excludeScopes = excludeScopes;
      return this;
    }

    public Builder withCurrentlyExecutingDelegateTasks(List<DelegateTask> currentlyExecutingDelegateTasks) {
      this.currentlyExecutingDelegateTasks = currentlyExecutingDelegateTasks;
      return this;
    }

    public Builder withUuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder withAppId(String appId) {
      this.appId = appId;
      return this;
    }

    public Builder withCreatedBy(EmbeddedUser createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public Builder withCreatedAt(long createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder withLastUpdatedBy(EmbeddedUser lastUpdatedBy) {
      this.lastUpdatedBy = lastUpdatedBy;
      return this;
    }

    public Builder withLastUpdatedAt(long lastUpdatedAt) {
      this.lastUpdatedAt = lastUpdatedAt;
      return this;
    }

    public Builder but() {
      return aDelegate()
          .withAccountId(accountId)
          .withStatus(status)
          .withConnected(connected)
          .withIp(ip)
          .withHostName(hostName)
          .withLastHeartBeat(lastHeartBeat)
          .withVersion(version)
          .withSupportedTaskTypes(supportedTaskTypes)
          .withIncludeScopes(includeScopes)
          .withExcludeScopes(excludeScopes)
          .withCurrentlyExecutingDelegateTasks(currentlyExecutingDelegateTasks)
          .withUuid(uuid)
          .withAppId(appId)
          .withCreatedBy(createdBy)
          .withCreatedAt(createdAt)
          .withLastUpdatedBy(lastUpdatedBy)
          .withLastUpdatedAt(lastUpdatedAt);
    }

    public Delegate build() {
      Delegate delegate = new Delegate();
      delegate.setAccountId(accountId);
      delegate.setStatus(status);
      delegate.setConnected(connected);
      delegate.setIp(ip);
      delegate.setHostName(hostName);
      delegate.setLastHeartBeat(lastHeartBeat);
      delegate.setVersion(version);
      delegate.setSupportedTaskTypes(supportedTaskTypes);
      delegate.setIncludeScopes(includeScopes);
      delegate.setExcludeScopes(excludeScopes);
      delegate.setCurrentlyExecutingDelegateTasks(currentlyExecutingDelegateTasks);
      delegate.setUuid(uuid);
      delegate.setAppId(appId);
      delegate.setCreatedBy(createdBy);
      delegate.setCreatedAt(createdAt);
      delegate.setLastUpdatedBy(lastUpdatedBy);
      delegate.setLastUpdatedAt(lastUpdatedAt);
      return delegate;
    }
  }

  @Override
  public String toString() {
    return "Delegate{"
        + "accountId='" + accountId + '\'' + ", status=" + status + ", connected=" + connected + ", ip='" + ip + '\''
        + ", hostName='" + hostName + '\'' + ", lastHeartBeat=" + lastHeartBeat + ", version='" + version + '\''
        + ", supportedTaskTypes=" + supportedTaskTypes + ", includeScopes=" + includeScopes
        + ", excludeScopes=" + excludeScopes + '}';
  }
}
