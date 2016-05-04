package software.wings.beans;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

import java.util.Objects;

/**
 * Application bean class.
 *
 * @author Rishi
 */
@Indexes(
    @Index(fields = { @Field("appId")
                      , @Field("name"), @Field("version") }, options = @IndexOptions(unique = true)))
@Entity(value = "platforms", noClassnameStored = true)
public class AppContainer extends BaseFile {
  private String appId;
  private boolean standard;
  private String version;
  private String description;
  private ArtifactSource source;
  private boolean standardUpload = false;

  public AppContainer() {}

  public AppContainer(String fileName, String md5) {
    super(fileName, md5);
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public boolean isStandard() {
    return standard;
  }

  public void setStandard(boolean standard) {
    this.standard = standard;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ArtifactSource getSource() {
    return source;
  }

  public void setSource(ArtifactSource source) {
    this.source = source;
  }

  public boolean isStandardUpload() {
    return standardUpload;
  }

  public void setStandardUpload(boolean standardUpload) {
    this.standardUpload = standardUpload;
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + Objects.hash(appId, standard, version, description, source, standardUpload);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final AppContainer other = (AppContainer) obj;
    return Objects.equals(this.appId, other.appId) && Objects.equals(this.standard, other.standard)
        && Objects.equals(this.version, other.version) && Objects.equals(this.description, other.description)
        && Objects.equals(this.source, other.source) && Objects.equals(this.standardUpload, other.standardUpload);
  }

  public static final class AppContainerBuilder {
    private String appId;
    private boolean standard;
    private String version;
    private String description;
    private ArtifactSource source;
    private boolean standardUpload = false;
    private String fileUuid;
    private String name;
    private String mimeType;
    private long size;
    private ChecksumType checksumType;
    private String checksum;
    private String uuid;
    private User createdBy;
    private long createdAt;
    private User lastUpdatedBy;
    private long lastUpdatedAt;
    private boolean active = true;

    private AppContainerBuilder() {}

    public static AppContainerBuilder anAppContainer() {
      return new AppContainerBuilder();
    }

    public AppContainerBuilder withAppId(String appId) {
      this.appId = appId;
      return this;
    }

    public AppContainerBuilder withStandard(boolean standard) {
      this.standard = standard;
      return this;
    }

    public AppContainerBuilder withVersion(String version) {
      this.version = version;
      return this;
    }

    public AppContainerBuilder withDescription(String description) {
      this.description = description;
      return this;
    }

    public AppContainerBuilder withSource(ArtifactSource source) {
      this.source = source;
      return this;
    }

    public AppContainerBuilder withStandardUpload(boolean standardUpload) {
      this.standardUpload = standardUpload;
      return this;
    }

    public AppContainerBuilder withFileUuid(String fileUuid) {
      this.fileUuid = fileUuid;
      return this;
    }

    public AppContainerBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public AppContainerBuilder withMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public AppContainerBuilder withSize(long size) {
      this.size = size;
      return this;
    }

    public AppContainerBuilder withChecksumType(ChecksumType checksumType) {
      this.checksumType = checksumType;
      return this;
    }

    public AppContainerBuilder withChecksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public AppContainerBuilder withUuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public AppContainerBuilder withCreatedBy(User createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public AppContainerBuilder withCreatedAt(long createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public AppContainerBuilder withLastUpdatedBy(User lastUpdatedBy) {
      this.lastUpdatedBy = lastUpdatedBy;
      return this;
    }

    public AppContainerBuilder withLastUpdatedAt(long lastUpdatedAt) {
      this.lastUpdatedAt = lastUpdatedAt;
      return this;
    }

    public AppContainerBuilder withActive(boolean active) {
      this.active = active;
      return this;
    }

    public AppContainerBuilder but() {
      return anAppContainer()
          .withAppId(appId)
          .withStandard(standard)
          .withVersion(version)
          .withDescription(description)
          .withSource(source)
          .withStandardUpload(standardUpload)
          .withFileUuid(fileUuid)
          .withName(name)
          .withMimeType(mimeType)
          .withSize(size)
          .withChecksumType(checksumType)
          .withChecksum(checksum)
          .withUuid(uuid)
          .withCreatedBy(createdBy)
          .withCreatedAt(createdAt)
          .withLastUpdatedBy(lastUpdatedBy)
          .withLastUpdatedAt(lastUpdatedAt)
          .withActive(active);
    }

    public AppContainer build() {
      AppContainer appContainer = new AppContainer();
      appContainer.setAppId(appId);
      appContainer.setStandard(standard);
      appContainer.setVersion(version);
      appContainer.setDescription(description);
      appContainer.setSource(source);
      appContainer.setStandardUpload(standardUpload);
      appContainer.setFileUuid(fileUuid);
      appContainer.setName(name);
      appContainer.setMimeType(mimeType);
      appContainer.setSize(size);
      appContainer.setChecksumType(checksumType);
      appContainer.setChecksum(checksum);
      appContainer.setUuid(uuid);
      appContainer.setCreatedBy(createdBy);
      appContainer.setCreatedAt(createdAt);
      appContainer.setLastUpdatedBy(lastUpdatedBy);
      appContainer.setLastUpdatedAt(lastUpdatedAt);
      appContainer.setActive(active);
      return appContainer;
    }
  }
}
