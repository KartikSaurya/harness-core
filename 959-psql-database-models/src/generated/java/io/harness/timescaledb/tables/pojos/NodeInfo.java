/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.pojos;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class NodeInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String accountid;
  private String clusterid;
  private String instanceid;
  private OffsetDateTime starttime;
  private OffsetDateTime stoptime;
  private String nodepoolname;
  private OffsetDateTime createdat;
  private OffsetDateTime updatedat;

  public NodeInfo() {}

  public NodeInfo(NodeInfo value) {
    this.accountid = value.accountid;
    this.clusterid = value.clusterid;
    this.instanceid = value.instanceid;
    this.starttime = value.starttime;
    this.stoptime = value.stoptime;
    this.nodepoolname = value.nodepoolname;
    this.createdat = value.createdat;
    this.updatedat = value.updatedat;
  }

  public NodeInfo(String accountid, String clusterid, String instanceid, OffsetDateTime starttime,
      OffsetDateTime stoptime, String nodepoolname, OffsetDateTime createdat, OffsetDateTime updatedat) {
    this.accountid = accountid;
    this.clusterid = clusterid;
    this.instanceid = instanceid;
    this.starttime = starttime;
    this.stoptime = stoptime;
    this.nodepoolname = nodepoolname;
    this.createdat = createdat;
    this.updatedat = updatedat;
  }

  /**
   * Getter for <code>public.node_info.accountid</code>.
   */
  public String getAccountid() {
    return this.accountid;
  }

  /**
   * Setter for <code>public.node_info.accountid</code>.
   */
  public NodeInfo setAccountid(String accountid) {
    this.accountid = accountid;
    return this;
  }

  /**
   * Getter for <code>public.node_info.clusterid</code>.
   */
  public String getClusterid() {
    return this.clusterid;
  }

  /**
   * Setter for <code>public.node_info.clusterid</code>.
   */
  public NodeInfo setClusterid(String clusterid) {
    this.clusterid = clusterid;
    return this;
  }

  /**
   * Getter for <code>public.node_info.instanceid</code>.
   */
  public String getInstanceid() {
    return this.instanceid;
  }

  /**
   * Setter for <code>public.node_info.instanceid</code>.
   */
  public NodeInfo setInstanceid(String instanceid) {
    this.instanceid = instanceid;
    return this;
  }

  /**
   * Getter for <code>public.node_info.starttime</code>.
   */
  public OffsetDateTime getStarttime() {
    return this.starttime;
  }

  /**
   * Setter for <code>public.node_info.starttime</code>.
   */
  public NodeInfo setStarttime(OffsetDateTime starttime) {
    this.starttime = starttime;
    return this;
  }

  /**
   * Getter for <code>public.node_info.stoptime</code>.
   */
  public OffsetDateTime getStoptime() {
    return this.stoptime;
  }

  /**
   * Setter for <code>public.node_info.stoptime</code>.
   */
  public NodeInfo setStoptime(OffsetDateTime stoptime) {
    this.stoptime = stoptime;
    return this;
  }

  /**
   * Getter for <code>public.node_info.nodepoolname</code>.
   */
  public String getNodepoolname() {
    return this.nodepoolname;
  }

  /**
   * Setter for <code>public.node_info.nodepoolname</code>.
   */
  public NodeInfo setNodepoolname(String nodepoolname) {
    this.nodepoolname = nodepoolname;
    return this;
  }

  /**
   * Getter for <code>public.node_info.createdat</code>.
   */
  public OffsetDateTime getCreatedat() {
    return this.createdat;
  }

  /**
   * Setter for <code>public.node_info.createdat</code>.
   */
  public NodeInfo setCreatedat(OffsetDateTime createdat) {
    this.createdat = createdat;
    return this;
  }

  /**
   * Getter for <code>public.node_info.updatedat</code>.
   */
  public OffsetDateTime getUpdatedat() {
    return this.updatedat;
  }

  /**
   * Setter for <code>public.node_info.updatedat</code>.
   */
  public NodeInfo setUpdatedat(OffsetDateTime updatedat) {
    this.updatedat = updatedat;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final NodeInfo other = (NodeInfo) obj;
    if (accountid == null) {
      if (other.accountid != null)
        return false;
    } else if (!accountid.equals(other.accountid))
      return false;
    if (clusterid == null) {
      if (other.clusterid != null)
        return false;
    } else if (!clusterid.equals(other.clusterid))
      return false;
    if (instanceid == null) {
      if (other.instanceid != null)
        return false;
    } else if (!instanceid.equals(other.instanceid))
      return false;
    if (starttime == null) {
      if (other.starttime != null)
        return false;
    } else if (!starttime.equals(other.starttime))
      return false;
    if (stoptime == null) {
      if (other.stoptime != null)
        return false;
    } else if (!stoptime.equals(other.stoptime))
      return false;
    if (nodepoolname == null) {
      if (other.nodepoolname != null)
        return false;
    } else if (!nodepoolname.equals(other.nodepoolname))
      return false;
    if (createdat == null) {
      if (other.createdat != null)
        return false;
    } else if (!createdat.equals(other.createdat))
      return false;
    if (updatedat == null) {
      if (other.updatedat != null)
        return false;
    } else if (!updatedat.equals(other.updatedat))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.accountid == null) ? 0 : this.accountid.hashCode());
    result = prime * result + ((this.clusterid == null) ? 0 : this.clusterid.hashCode());
    result = prime * result + ((this.instanceid == null) ? 0 : this.instanceid.hashCode());
    result = prime * result + ((this.starttime == null) ? 0 : this.starttime.hashCode());
    result = prime * result + ((this.stoptime == null) ? 0 : this.stoptime.hashCode());
    result = prime * result + ((this.nodepoolname == null) ? 0 : this.nodepoolname.hashCode());
    result = prime * result + ((this.createdat == null) ? 0 : this.createdat.hashCode());
    result = prime * result + ((this.updatedat == null) ? 0 : this.updatedat.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("NodeInfo (");

    sb.append(accountid);
    sb.append(", ").append(clusterid);
    sb.append(", ").append(instanceid);
    sb.append(", ").append(starttime);
    sb.append(", ").append(stoptime);
    sb.append(", ").append(nodepoolname);
    sb.append(", ").append(createdat);
    sb.append(", ").append(updatedat);

    sb.append(")");
    return sb.toString();
  }
}
