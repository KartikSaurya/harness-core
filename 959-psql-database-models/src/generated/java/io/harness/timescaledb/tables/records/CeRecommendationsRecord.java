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

import io.harness.timescaledb.tables.CeRecommendations;

import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class CeRecommendationsRecord extends UpdatableRecordImpl<CeRecommendationsRecord> implements Record11<String,
    String, String, Double, Double, String, String, String, Boolean, OffsetDateTime, OffsetDateTime> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.ce_recommendations.id</code>.
   */
  public CeRecommendationsRecord setId(String value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.id</code>.
   */
  public String getId() {
    return (String) get(0);
  }

  /**
   * Setter for <code>public.ce_recommendations.name</code>.
   */
  public CeRecommendationsRecord setName(String value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.name</code>.
   */
  public String getName() {
    return (String) get(1);
  }

  /**
   * Setter for <code>public.ce_recommendations.namespace</code>.
   */
  public CeRecommendationsRecord setNamespace(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.namespace</code>.
   */
  public String getNamespace() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.ce_recommendations.monthlycost</code>.
   */
  public CeRecommendationsRecord setMonthlycost(Double value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.monthlycost</code>.
   */
  public Double getMonthlycost() {
    return (Double) get(3);
  }

  /**
   * Setter for <code>public.ce_recommendations.monthlysaving</code>.
   */
  public CeRecommendationsRecord setMonthlysaving(Double value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.monthlysaving</code>.
   */
  public Double getMonthlysaving() {
    return (Double) get(4);
  }

  /**
   * Setter for <code>public.ce_recommendations.clustername</code>.
   */
  public CeRecommendationsRecord setClustername(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.clustername</code>.
   */
  public String getClustername() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.ce_recommendations.resourcetype</code>.
   */
  public CeRecommendationsRecord setResourcetype(String value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.resourcetype</code>.
   */
  public String getResourcetype() {
    return (String) get(6);
  }

  /**
   * Setter for <code>public.ce_recommendations.accountid</code>.
   */
  public CeRecommendationsRecord setAccountid(String value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.accountid</code>.
   */
  public String getAccountid() {
    return (String) get(7);
  }

  /**
   * Setter for <code>public.ce_recommendations.isvalid</code>.
   */
  public CeRecommendationsRecord setIsvalid(Boolean value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.isvalid</code>.
   */
  public Boolean getIsvalid() {
    return (Boolean) get(8);
  }

  /**
   * Setter for <code>public.ce_recommendations.lastprocessedat</code>.
   */
  public CeRecommendationsRecord setLastprocessedat(OffsetDateTime value) {
    set(9, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.lastprocessedat</code>.
   */
  public OffsetDateTime getLastprocessedat() {
    return (OffsetDateTime) get(9);
  }

  /**
   * Setter for <code>public.ce_recommendations.updatedat</code>.
   */
  public CeRecommendationsRecord setUpdatedat(OffsetDateTime value) {
    set(10, value);
    return this;
  }

  /**
   * Getter for <code>public.ce_recommendations.updatedat</code>.
   */
  public OffsetDateTime getUpdatedat() {
    return (OffsetDateTime) get(10);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record1<String> key() {
    return (Record1) super.key();
  }

  // -------------------------------------------------------------------------
  // Record11 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row11<String, String, String, Double, Double, String, String, String, Boolean, OffsetDateTime, OffsetDateTime>
  fieldsRow() {
    return (Row11) super.fieldsRow();
  }

  @Override
  public Row11<String, String, String, Double, Double, String, String, String, Boolean, OffsetDateTime, OffsetDateTime>
  valuesRow() {
    return (Row11) super.valuesRow();
  }

  @Override
  public Field<String> field1() {
    return CeRecommendations.CE_RECOMMENDATIONS.ID;
  }

  @Override
  public Field<String> field2() {
    return CeRecommendations.CE_RECOMMENDATIONS.NAME;
  }

  @Override
  public Field<String> field3() {
    return CeRecommendations.CE_RECOMMENDATIONS.NAMESPACE;
  }

  @Override
  public Field<Double> field4() {
    return CeRecommendations.CE_RECOMMENDATIONS.MONTHLYCOST;
  }

  @Override
  public Field<Double> field5() {
    return CeRecommendations.CE_RECOMMENDATIONS.MONTHLYSAVING;
  }

  @Override
  public Field<String> field6() {
    return CeRecommendations.CE_RECOMMENDATIONS.CLUSTERNAME;
  }

  @Override
  public Field<String> field7() {
    return CeRecommendations.CE_RECOMMENDATIONS.RESOURCETYPE;
  }

  @Override
  public Field<String> field8() {
    return CeRecommendations.CE_RECOMMENDATIONS.ACCOUNTID;
  }

  @Override
  public Field<Boolean> field9() {
    return CeRecommendations.CE_RECOMMENDATIONS.ISVALID;
  }

  @Override
  public Field<OffsetDateTime> field10() {
    return CeRecommendations.CE_RECOMMENDATIONS.LASTPROCESSEDAT;
  }

  @Override
  public Field<OffsetDateTime> field11() {
    return CeRecommendations.CE_RECOMMENDATIONS.UPDATEDAT;
  }

  @Override
  public String component1() {
    return getId();
  }

  @Override
  public String component2() {
    return getName();
  }

  @Override
  public String component3() {
    return getNamespace();
  }

  @Override
  public Double component4() {
    return getMonthlycost();
  }

  @Override
  public Double component5() {
    return getMonthlysaving();
  }

  @Override
  public String component6() {
    return getClustername();
  }

  @Override
  public String component7() {
    return getResourcetype();
  }

  @Override
  public String component8() {
    return getAccountid();
  }

  @Override
  public Boolean component9() {
    return getIsvalid();
  }

  @Override
  public OffsetDateTime component10() {
    return getLastprocessedat();
  }

  @Override
  public OffsetDateTime component11() {
    return getUpdatedat();
  }

  @Override
  public String value1() {
    return getId();
  }

  @Override
  public String value2() {
    return getName();
  }

  @Override
  public String value3() {
    return getNamespace();
  }

  @Override
  public Double value4() {
    return getMonthlycost();
  }

  @Override
  public Double value5() {
    return getMonthlysaving();
  }

  @Override
  public String value6() {
    return getClustername();
  }

  @Override
  public String value7() {
    return getResourcetype();
  }

  @Override
  public String value8() {
    return getAccountid();
  }

  @Override
  public Boolean value9() {
    return getIsvalid();
  }

  @Override
  public OffsetDateTime value10() {
    return getLastprocessedat();
  }

  @Override
  public OffsetDateTime value11() {
    return getUpdatedat();
  }

  @Override
  public CeRecommendationsRecord value1(String value) {
    setId(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value2(String value) {
    setName(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value3(String value) {
    setNamespace(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value4(Double value) {
    setMonthlycost(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value5(Double value) {
    setMonthlysaving(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value6(String value) {
    setClustername(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value7(String value) {
    setResourcetype(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value8(String value) {
    setAccountid(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value9(Boolean value) {
    setIsvalid(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value10(OffsetDateTime value) {
    setLastprocessedat(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord value11(OffsetDateTime value) {
    setUpdatedat(value);
    return this;
  }

  @Override
  public CeRecommendationsRecord values(String value1, String value2, String value3, Double value4, Double value5,
      String value6, String value7, String value8, Boolean value9, OffsetDateTime value10, OffsetDateTime value11) {
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
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached CeRecommendationsRecord
   */
  public CeRecommendationsRecord() {
    super(CeRecommendations.CE_RECOMMENDATIONS);
  }

  /**
   * Create a detached, initialised CeRecommendationsRecord
   */
  public CeRecommendationsRecord(String id, String name, String namespace, Double monthlycost, Double monthlysaving,
      String clustername, String resourcetype, String accountid, Boolean isvalid, OffsetDateTime lastprocessedat,
      OffsetDateTime updatedat) {
    super(CeRecommendations.CE_RECOMMENDATIONS);

    setId(id);
    setName(name);
    setNamespace(namespace);
    setMonthlycost(monthlycost);
    setMonthlysaving(monthlysaving);
    setClustername(clustername);
    setResourcetype(resourcetype);
    setAccountid(accountid);
    setIsvalid(isvalid);
    setLastprocessedat(lastprocessedat);
    setUpdatedat(updatedat);
  }
}
