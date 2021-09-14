-- Copyright 2021 Harness Inc.
-- 
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

BEGIN;
DELETE FROM COST_EVENT_DATA;
CREATE UNIQUE INDEX IF NOT EXISTS COST_EVENT_KUBERNETES_UNIQUE_INDEX ON COST_EVENT_DATA(ACCOUNTID, CLUSTERID, INSTANCEID, COSTEVENTTYPE, STARTTIME DESC);
CREATE UNIQUE INDEX IF NOT EXISTS COST_EVENT_DEPLOYMENT_UNIQUE_INDEX ON COST_EVENT_DATA(ACCOUNTID, DEPLOYMENTID, STARTTIME DESC);
CREATE INDEX IF NOT EXISTS COST_EVENT_DATA_NAMESPACE_INDEX ON COST_EVENT_DATA(ACCOUNTID, CLUSTERID, NAMESPACE, WORKLOADNAME, STARTTIME DESC);
COMMIT;
