package io.harness.notification.service;

import static io.harness.NotificationServiceConstants.*;

import io.harness.NotificationRequest;
import io.harness.notification.NotificationChannelType;
import io.harness.notification.remote.dto.NotificationSettingDTO;
import io.harness.notification.service.api.ChannelService;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.EnumMap;
import java.util.Map;

public class ChannelServiceImpl implements ChannelService {
  Map<NotificationChannelType, ChannelService> implementationMapping = new EnumMap<>(NotificationChannelType.class);
  Map<NotificationRequest.ChannelCase, ChannelService> protoImplementationMapping =
      new EnumMap<>(NotificationRequest.ChannelCase.class);

  @Inject
  public ChannelServiceImpl(@Named(MAILSERVICE) ChannelService mailService,
      @Named(SLACKSERVICE) ChannelService slackService, @Named(PAGERDUTYSERVICE) ChannelService pagerDutyService,
      @Named(MSTEAMSSERVICE) ChannelService msTeamsService) {
    implementationMapping.put(NotificationChannelType.EMAIL, mailService);
    implementationMapping.put(NotificationChannelType.PAGERDUTY, pagerDutyService);
    implementationMapping.put(NotificationChannelType.SLACK, slackService);
    implementationMapping.put(NotificationChannelType.MSTEAMS, msTeamsService);

    protoImplementationMapping.put(NotificationRequest.ChannelCase.EMAIL, mailService);
    protoImplementationMapping.put(NotificationRequest.ChannelCase.PAGERDUTY, pagerDutyService);
    protoImplementationMapping.put(NotificationRequest.ChannelCase.SLACK, slackService);
    protoImplementationMapping.put(NotificationRequest.ChannelCase.MSTEAM, msTeamsService);
  }

  @Override
  public boolean send(NotificationRequest notificationRequest) {
    return protoImplementationMapping.get(notificationRequest.getChannelCase()).send(notificationRequest);
  }

  @Override
  public boolean sendTestNotification(NotificationSettingDTO notificationSettingDTO) {
    return implementationMapping.get(notificationSettingDTO.getType()).sendTestNotification(notificationSettingDTO);
  }
}
