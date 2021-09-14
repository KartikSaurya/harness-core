/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.notification.service.senders;

import io.harness.notification.beans.NotificationProcessingResponse;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class SlackSenderImpl {
  public static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient client;

  public SlackSenderImpl(OkHttpClient client) {
    this.client = client;
  }

  public SlackSenderImpl() {
    client = new OkHttpClient();
  }

  public NotificationProcessingResponse send(List<String> slackWebhookUrls, String message, String notificationId) {
    List<Boolean> results = new ArrayList<>();
    for (String webhookUrl : slackWebhookUrls) {
      boolean ret = sendJSONMessage(message, webhookUrl);
      results.add(ret);
    }
    return NotificationProcessingResponse.builder().result(results).build();
  }

  private boolean sendJSONMessage(String message, String slackWebhook) {
    try {
      RequestBody body = RequestBody.create(APPLICATION_JSON, message);
      Request request = new Request.Builder()
                            .url(slackWebhook)
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Accept", "*/*")
                            .addHeader("Cache-Control", "no-cache")
                            .addHeader("Host", "hooks.slack.com")
                            .addHeader("accept-encoding", "gzip, deflate")
                            .addHeader("content-length", "798")
                            .addHeader("Connection", "keep-alive")
                            .addHeader("cache-control", "no-cache")
                            .build();

      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          String bodyString = (null != response.body()) ? response.body().string() : "null";
          log.error("Response not Successful. Response body: {}", bodyString);
          return false;
        }
        return true;
      }
    } catch (Exception e) {
      log.error("Error sending post data", e);
    }
    return false;
  }
}
