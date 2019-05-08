package software.wings.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.reinert.jjschema.SchemaIgnore;
import io.harness.annotation.HarnessExportableEntity;
import io.harness.beans.EmbeddedUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

@Entity(value = "delegates", noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Indexes({ @Index(fields = { @Field("accountId") }, options = @IndexOptions(name = "delegateAccountIdIdx")) })
@HarnessExportableEntity
@FieldNameConstants(innerTypeName = "DelegateKeys")
public class Delegate extends Base {
  // Will be used by ECS delegate, when hostName is mentioned in TaskSpec.
  @NotEmpty private String accountId;
  private Status status = Status.ENABLED;
  private String description;
  private boolean connected;
  private String ip;
  private String hostName;
  private String delegateGroupName;
  private String delegateName;
  private String delegateProfileId;
  private long lastHeartBeat;
  private String version;
  private transient String sequenceNum;
  private String delegateType;
  private transient String delegateRandomToken;
  private transient boolean keepAlivePacket;
  private String verificationServiceSecret;
  private transient boolean polllingModeEnabled;

  @Deprecated private List<String> supportedTaskTypes;

  @Transient private List<String> currentlyExecutingDelegateTasks;

  private List<DelegateScope> includeScopes;
  private List<DelegateScope> excludeScopes;
  private List<String> tags;
  private String profileResult;
  private boolean profileError;
  private long profileExecutedAt;

  @SchemaIgnore private List<String> keywords;

  public enum Status { ENABLED, DISABLED, DELETED }

  public static final class Builder {
    private String accountId;
    private Status status = Status.ENABLED;
    private boolean connected;
    private String ip;
    private String description;
    private String hostName;
    private String delegateName;
    private String delegateGroupName;
    private String delegateProfileId;
    private long lastHeartBeat;
    private String version;
    private String sequenceNum;
    private String delegateType;
    private String delegateRandomToken;
    private boolean keepAlivePacket;
    private boolean polllingModeEnabled;

    private List<DelegateScope> includeScopes;
    private List<DelegateScope> excludeScopes;
    private List<String> currentlyExecutingDelegateTasks;
    private String uuid;
    private String appId;
    private EmbeddedUser createdBy;
    private long createdAt;
    private EmbeddedUser lastUpdatedBy;
    private long lastUpdatedAt;
    private List<String> tags;

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

    public Builder withTags(List<String> tags) {
      this.tags = tags;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
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

    public Builder withDelegateName(String delegateName) {
      this.delegateName = delegateName;
      return this;
    }

    public Builder withDelegateGroupName(String delegateGroupName) {
      this.delegateGroupName = delegateGroupName;
      return this;
    }

    public Builder withDelegateProfileId(String delegateProfileId) {
      this.delegateProfileId = delegateProfileId;
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

    public Builder withSequenceNum(String sequenceNum) {
      this.sequenceNum = sequenceNum;
      return this;
    }

    public Builder withDelegateType(String delegateType) {
      this.delegateType = delegateType;
      return this;
    }

    public Builder withDelegateRandomToken(String delegateRandomToken) {
      this.delegateRandomToken = delegateRandomToken;
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

    public Builder withCurrentlyExecutingDelegateTasks(List<String> currentlyExecutingDelegateTasks) {
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

    public Builder withKeepAlivePacket(boolean keepAlivePacket) {
      this.keepAlivePacket = keepAlivePacket;
      return this;
    }

    public Builder withPolllingModeEnabled(boolean polllingModeEnabled) {
      this.polllingModeEnabled = polllingModeEnabled;
      return this;
    }

    public Builder but() {
      return aDelegate()
          .withAccountId(accountId)
          .withStatus(status)
          .withDescription(description)
          .withConnected(connected)
          .withIp(ip)
          .withHostName(hostName)
          .withDelegateName(delegateName)
          .withDelegateGroupName(delegateGroupName)
          .withDelegateProfileId(delegateProfileId)
          .withLastHeartBeat(lastHeartBeat)
          .withVersion(version)
          .withSequenceNum(sequenceNum)
          .withDelegateType(delegateType)
          .withDelegateRandomToken(delegateRandomToken)
          .withIncludeScopes(includeScopes)
          .withExcludeScopes(excludeScopes)
          .withTags(tags)
          .withCurrentlyExecutingDelegateTasks(currentlyExecutingDelegateTasks)
          .withUuid(uuid)
          .withAppId(appId)
          .withCreatedBy(createdBy)
          .withCreatedAt(createdAt)
          .withLastUpdatedBy(lastUpdatedBy)
          .withLastUpdatedAt(lastUpdatedAt)
          .withKeepAlivePacket(keepAlivePacket)
          .withPolllingModeEnabled(polllingModeEnabled);
    }

    public Delegate build() {
      Delegate delegate = new Delegate();
      delegate.setAccountId(accountId);
      delegate.setStatus(status);
      delegate.setDescription(description);
      delegate.setConnected(connected);
      delegate.setIp(ip);
      delegate.setHostName(hostName);
      delegate.setDelegateName(delegateName);
      delegate.setDelegateGroupName(delegateGroupName);
      delegate.setDelegateProfileId(delegateProfileId);
      delegate.setLastHeartBeat(lastHeartBeat);
      delegate.setVersion(version);
      delegate.setSequenceNum(sequenceNum);
      delegate.setDelegateType(delegateType);
      delegate.setIncludeScopes(includeScopes);
      delegate.setExcludeScopes(excludeScopes);
      delegate.setTags(tags);
      delegate.setCurrentlyExecutingDelegateTasks(currentlyExecutingDelegateTasks);
      delegate.setUuid(uuid);
      delegate.setAppId(appId);
      delegate.setCreatedBy(createdBy);
      delegate.setCreatedAt(createdAt);
      delegate.setLastUpdatedBy(lastUpdatedBy);
      delegate.setLastUpdatedAt(lastUpdatedAt);
      delegate.setDelegateRandomToken(delegateRandomToken);
      delegate.setKeepAlivePacket(keepAlivePacket);
      delegate.setPolllingModeEnabled(polllingModeEnabled);
      return delegate;
    }
  }
}
