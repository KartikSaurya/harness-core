-- Copyright 2021 Harness Inc.
-- 
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

---------- INSTANCE_STATS TABLE START ------------
BEGIN;
CREATE TABLE IF NOT EXISTS NG_INSTANCE_STATS (
	REPORTEDAT TIMESTAMPTZ NOT NULL,
	ACCOUNTID TEXT,
	ORGID TEXT,
	PROJECTID TEXT,
	SERVICEID TEXT,
	ENVID TEXT,
	CLOUDPROVIDERID TEXT,
	INSTANCETYPE TEXT,
	INSTANCECOUNT INTEGER
);
COMMIT;
SELECT CREATE_HYPERTABLE('NG_INSTANCE_STATS','reportedat',if_not_exists => TRUE);

BEGIN;
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_ACCOUNTID_INDEX ON NG_INSTANCE_STATS(ACCOUNTID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_ORGID_INDEX ON NG_INSTANCE_STATS(ORGID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_PROJECTID_INDEX ON NG_INSTANCE_STATS(PROJECTID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_SERVICEID_INDEX ON NG_INSTANCE_STATS(SERVICEID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_ENVID_INDEX ON NG_INSTANCE_STATS(ENVID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_CLOUDPROVIDERID_INDEX ON NG_INSTANCE_STATS(CLOUDPROVIDERID,REPORTEDAT DESC);
CREATE INDEX IF NOT EXISTS NG_INSTANCE_STATS_INSTANCECOUNT_INDEX ON NG_INSTANCE_STATS(INSTANCECOUNT,REPORTEDAT DESC);
COMMIT;

---------- INSTANCE_STATS TABLE END ------------
