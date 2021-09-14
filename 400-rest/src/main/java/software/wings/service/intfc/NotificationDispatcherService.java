/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package software.wings.service.intfc;

import io.harness.beans.EmbeddedUser;

import software.wings.beans.Notification;
import software.wings.beans.NotificationRule;
import software.wings.beans.alert.AlertNotificationRule;
import software.wings.helpers.ext.mail.EmailData;

import java.util.List;
import java.util.Map;

/**
 * Created by rishi on 10/30/16.
 */
public interface NotificationDispatcherService {
  void dispatchNotification(Notification notification, List<NotificationRule> notificationRuleList);

  void dispatch(Notification notification, List<AlertNotificationRule> alertNotificationRules);

  EmailData obtainEmailData(String notificationTemplateId, Map<String, String> placeholderValues);

  void dispatchNotificationToTriggeredByUserOnly(List<Notification> notifications, EmbeddedUser user);
}
