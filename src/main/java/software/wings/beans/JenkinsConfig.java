package software.wings.beans;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

// TODO: Auto-generated Javadoc

/**
 * Created by peeyushaggarwal on 5/26/16.
 */
public class JenkinsConfig extends SettingValue {
  @URL private String jenkinsUrl;
  @NotEmpty private String username;
  @NotEmpty private String password;

  /**
   * Instantiates a new jenkins config.
   */
  public JenkinsConfig() {
    super(SettingVariableTypes.JENKINS);
  }

  public String getJenkinsUrl() {
    return jenkinsUrl;
  }

  public void setJenkinsUrl(String jenkinsUrl) {
    this.jenkinsUrl = jenkinsUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    JenkinsConfig that = (JenkinsConfig) o;
    return Objects.equal(jenkinsUrl, that.jenkinsUrl) && Objects.equal(username, that.username)
        && Objects.equal(password, that.password);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(jenkinsUrl, username, password);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("jenkinsUrl", jenkinsUrl)
        .add("username", username)
        .add("password", password)
        .toString();
  }

  /**
   * The Class Builder.
   */
  public static final class Builder {
    private String jenkinsUrl;
    private String username;
    private String password;

    private Builder() {}

    /**
     * A jenkins config.
     *
     * @return the builder
     */
    public static Builder aJenkinsConfig() {
      return new Builder();
    }

    /**
     * With jenkins url.
     *
     * @param jenkinsUrl the jenkins url
     * @return the builder
     */
    public Builder withJenkinsUrl(String jenkinsUrl) {
      this.jenkinsUrl = jenkinsUrl;
      return this;
    }

    /**
     * With username.
     *
     * @param username the username
     * @return the builder
     */
    public Builder withUsername(String username) {
      this.username = username;
      return this;
    }

    /**
     * With password.
     *
     * @param password the password
     * @return the builder
     */
    public Builder withPassword(String password) {
      this.password = password;
      return this;
    }

    /**
     * But.
     *
     * @return the builder
     */
    public Builder but() {
      return aJenkinsConfig().withJenkinsUrl(jenkinsUrl).withUsername(username).withPassword(password);
    }

    /**
     * Builds the.
     *
     * @return the jenkins config
     */
    public JenkinsConfig build() {
      JenkinsConfig jenkinsConfig = new JenkinsConfig();
      jenkinsConfig.setJenkinsUrl(jenkinsUrl);
      jenkinsConfig.setUsername(username);
      jenkinsConfig.setPassword(password);
      return jenkinsConfig;
    }
  }
}
