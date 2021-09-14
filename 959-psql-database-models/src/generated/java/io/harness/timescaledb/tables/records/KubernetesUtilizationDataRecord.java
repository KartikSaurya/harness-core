/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.records;

import io.harness.timescaledb.tables.KubernetesUtilizationData;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Record14;
import org.jooq.Row14;
import org.jooq.impl.TableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class KubernetesUtilizationDataRecord
    extends TableRecordImpl<KubernetesUtilizationDataRecord> implements Record14<OffsetDateTime, OffsetDateTime, String,
        String, String, String, Double, Double, String, Double, Double, Double, Double, String> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.kubernetes_utilization_data.starttime</code>.
   */
  public KubernetesUtilizationDataRecord setStarttime(OffsetDateTime value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.starttime</code>.
   */
  public OffsetDateTime getStarttime() {
    return (OffsetDateTime) get(0);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.endtime</code>.
   */
  public KubernetesUtilizationDataRecord setEndtime(OffsetDateTime value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.endtime</code>.
   */
  public OffsetDateTime getEndtime() {
    return (OffsetDateTime) get(1);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.accountid</code>.
   */
  public KubernetesUtilizationDataRecord setAccountid(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.accountid</code>.
   */
  public String getAccountid() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.settingid</code>.
   */
  public KubernetesUtilizationDataRecord setSettingid(String value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.settingid</code>.
   */
  public String getSettingid() {
    return (String) get(3);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.instanceid</code>.
   */
  public KubernetesUtilizationDataRecord setInstanceid(String value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.instanceid</code>.
   */
  public String getInstanceid() {
    return (String) get(4);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.instancetype</code>.
   */
  public KubernetesUtilizationDataRecord setInstancetype(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.instancetype</code>.
   */
  public String getInstancetype() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.cpu</code>.
   */
  public KubernetesUtilizationDataRecord setCpu(Double value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.cpu</code>.
   */
  public Double getCpu() {
    return (Double) get(6);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.memory</code>.
   */
  public KubernetesUtilizationDataRecord setMemory(Double value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.memory</code>.
   */
  public Double getMemory() {
    return (Double) get(7);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.clusterid</code>.
   */
  public KubernetesUtilizationDataRecord setClusterid(String value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.clusterid</code>.
   */
  public String getClusterid() {
    return (String) get(8);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.maxcpu</code>.
   */
  public KubernetesUtilizationDataRecord setMaxcpu(Double value) {
    set(9, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.maxcpu</code>.
   */
  public Double getMaxcpu() {
    return (Double) get(9);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.maxmemory</code>.
   */
  public KubernetesUtilizationDataRecord setMaxmemory(Double value) {
    set(10, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.maxmemory</code>.
   */
  public Double getMaxmemory() {
    return (Double) get(10);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.storagerequestvalue</code>.
   */
  public KubernetesUtilizationDataRecord setStoragerequestvalue(Double value) {
    set(11, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.storagerequestvalue</code>.
   */
  public Double getStoragerequestvalue() {
    return (Double) get(11);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.storageusagevalue</code>.
   */
  public KubernetesUtilizationDataRecord setStorageusagevalue(Double value) {
    set(12, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.storageusagevalue</code>.
   */
  public Double getStorageusagevalue() {
    return (Double) get(12);
  }

  /**
   * Setter for <code>public.kubernetes_utilization_data.actualinstanceid</code>.
   */
  public KubernetesUtilizationDataRecord setActualinstanceid(String value) {
    set(13, value);
    return this;
  }

  /**
   * Getter for <code>public.kubernetes_utilization_data.actualinstanceid</code>.
   */
  public String getActualinstanceid() {
    return (String) get(13);
  }

  // -------------------------------------------------------------------------
  // Record14 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row14<OffsetDateTime, OffsetDateTime, String, String, String, String, Double, Double, String, Double, Double,
      Double, Double, String>
  fieldsRow() {
    return (Row14) super.fieldsRow();
  }

  @Override
  public Row14<OffsetDateTime, OffsetDateTime, String, String, String, String, Double, Double, String, Double, Double,
      Double, Double, String>
  valuesRow() {
    return (Row14) super.valuesRow();
  }

  @Override
  public Field<OffsetDateTime> field1() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.STARTTIME;
  }

  @Override
  public Field<OffsetDateTime> field2() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.ENDTIME;
  }

  @Override
  public Field<String> field3() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.ACCOUNTID;
  }

  @Override
  public Field<String> field4() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.SETTINGID;
  }

  @Override
  public Field<String> field5() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.INSTANCEID;
  }

  @Override
  public Field<String> field6() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.INSTANCETYPE;
  }

  @Override
  public Field<Double> field7() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.CPU;
  }

  @Override
  public Field<Double> field8() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.MEMORY;
  }

  @Override
  public Field<String> field9() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.CLUSTERID;
  }

  @Override
  public Field<Double> field10() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.MAXCPU;
  }

  @Override
  public Field<Double> field11() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.MAXMEMORY;
  }

  @Override
  public Field<Double> field12() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.STORAGEREQUESTVALUE;
  }

  @Override
  public Field<Double> field13() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.STORAGEUSAGEVALUE;
  }

  @Override
  public Field<String> field14() {
    return KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA.ACTUALINSTANCEID;
  }

  @Override
  public OffsetDateTime component1() {
    return getStarttime();
  }

  @Override
  public OffsetDateTime component2() {
    return getEndtime();
  }

  @Override
  public String component3() {
    return getAccountid();
  }

  @Override
  public String component4() {
    return getSettingid();
  }

  @Override
  public String component5() {
    return getInstanceid();
  }

  @Override
  public String component6() {
    return getInstancetype();
  }

  @Override
  public Double component7() {
    return getCpu();
  }

  @Override
  public Double component8() {
    return getMemory();
  }

  @Override
  public String component9() {
    return getClusterid();
  }

  @Override
  public Double component10() {
    return getMaxcpu();
  }

  @Override
  public Double component11() {
    return getMaxmemory();
  }

  @Override
  public Double component12() {
    return getStoragerequestvalue();
  }

  @Override
  public Double component13() {
    return getStorageusagevalue();
  }

  @Override
  public String component14() {
    return getActualinstanceid();
  }

  @Override
  public OffsetDateTime value1() {
    return getStarttime();
  }

  @Override
  public OffsetDateTime value2() {
    return getEndtime();
  }

  @Override
  public String value3() {
    return getAccountid();
  }

  @Override
  public String value4() {
    return getSettingid();
  }

  @Override
  public String value5() {
    return getInstanceid();
  }

  @Override
  public String value6() {
    return getInstancetype();
  }

  @Override
  public Double value7() {
    return getCpu();
  }

  @Override
  public Double value8() {
    return getMemory();
  }

  @Override
  public String value9() {
    return getClusterid();
  }

  @Override
  public Double value10() {
    return getMaxcpu();
  }

  @Override
  public Double value11() {
    return getMaxmemory();
  }

  @Override
  public Double value12() {
    return getStoragerequestvalue();
  }

  @Override
  public Double value13() {
    return getStorageusagevalue();
  }

  @Override
  public String value14() {
    return getActualinstanceid();
  }

  @Override
  public KubernetesUtilizationDataRecord value1(OffsetDateTime value) {
    setStarttime(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value2(OffsetDateTime value) {
    setEndtime(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value3(String value) {
    setAccountid(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value4(String value) {
    setSettingid(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value5(String value) {
    setInstanceid(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value6(String value) {
    setInstancetype(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value7(Double value) {
    setCpu(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value8(Double value) {
    setMemory(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value9(String value) {
    setClusterid(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value10(Double value) {
    setMaxcpu(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value11(Double value) {
    setMaxmemory(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value12(Double value) {
    setStoragerequestvalue(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value13(Double value) {
    setStorageusagevalue(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord value14(String value) {
    setActualinstanceid(value);
    return this;
  }

  @Override
  public KubernetesUtilizationDataRecord values(OffsetDateTime value1, OffsetDateTime value2, String value3,
      String value4, String value5, String value6, Double value7, Double value8, String value9, Double value10,
      Double value11, Double value12, Double value13, String value14) {
    value1(value1);
    value2(value2);
    value3(value3);
    value4(value4);
    value5(value5);
    value6(value6);
    value7(value7);
    value8(value8);
    value9(value9);
    value10(value10);
    value11(value11);
    value12(value12);
    value13(value13);
    value14(value14);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached KubernetesUtilizationDataRecord
   */
  public KubernetesUtilizationDataRecord() {
    super(KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA);
  }

  /**
   * Create a detached, initialised KubernetesUtilizationDataRecord
   */
  public KubernetesUtilizationDataRecord(OffsetDateTime starttime, OffsetDateTime endtime, String accountid,
      String settingid, String instanceid, String instancetype, Double cpu, Double memory, String clusterid,
      Double maxcpu, Double maxmemory, Double storagerequestvalue, Double storageusagevalue, String actualinstanceid) {
    super(KubernetesUtilizationData.KUBERNETES_UTILIZATION_DATA);

    setStarttime(starttime);
    setEndtime(endtime);
    setAccountid(accountid);
    setSettingid(settingid);
    setInstanceid(instanceid);
    setInstancetype(instancetype);
    setCpu(cpu);
    setMemory(memory);
    setClusterid(clusterid);
    setMaxcpu(maxcpu);
    setMaxmemory(maxmemory);
    setStoragerequestvalue(storagerequestvalue);
    setStorageusagevalue(storageusagevalue);
    setActualinstanceid(actualinstanceid);
  }
}
