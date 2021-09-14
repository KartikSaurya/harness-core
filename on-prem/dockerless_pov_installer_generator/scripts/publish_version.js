/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

print(' ');
print('Setting manager primary version: ' + "VERSION");
const configQuery = { "_id": "__GLOBAL_CONFIG_ID__" };
if (db.managerConfiguration.find(configQuery).count() == 0) {
  db.managerConfiguration.insert({
      "_id" : "__GLOBAL_CONFIG_ID__",
      "primaryVersion" : "VERSION",
      "createdAt" : NumberLong(1531375566703),
      "lastUpdatedAt" : NumberLong(1531375566703)
  });
} else {
  const setPrimary = { $set: { "primaryVersion": "VERSION" } };
  const primaryResult = db.managerConfiguration.findAndModify({ query: configQuery, update: setPrimary });
}
const mgrConfig = db.managerConfiguration.find(configQuery);
print('Manager primary version is ' + mgrConfig.next().primaryVersion);

print(' ');
print('Publishing delegate version: ' + "VERSION");
const accountQuery = { "_id": "__GLOBAL_ACCOUNT_ID__" };
if (db.accounts.find(accountQuery).count() == 0) {
  db.accounts.insert({
      "_id" : "__GLOBAL_ACCOUNT_ID__",
      "companyName" : "Global",
      "accountName" : "Global",
      "delegateConfiguration" : {
          "delegateVersions" : [
              "VERSION"
          ]
      }
   });
} else {
  const setDelegate = { $set: { "delegateConfiguration.delegateVersions": Array.from(new Set(["VERSION"])) } };
  const delegateResult = db.accounts.findAndModify({ query: accountQuery, update: setDelegate });
}
const account = db.accounts.find(accountQuery);
print('Published delegate version: ' + account.next().delegateConfiguration.delegateVersions);
print(' ');
