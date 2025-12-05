/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.api.data.uinotification.TopicDo;
import org.eclipse.scout.rt.api.data.uinotification.UiNotificationDo;
import org.eclipse.scout.rt.api.data.uinotification.UiNotificationRequest;
import org.eclipse.scout.rt.api.data.uinotification.UiNotificationResponse;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.IRestResource;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("ui-notifications")
public class UiNotificationResource implements IRestResource {
  private static final Logger LOG = LoggerFactory.getLogger(UiNotificationResource.class);

  private UiNotificationRegistry m_registry;

  public UiNotificationResource() {
    m_registry = BEANS.get(UiNotificationRegistry.class);
  }

  protected UiNotificationRegistry getRegistry() {
    return m_registry;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiExposed
  public void get(UiNotificationRequest request, @Suspended AsyncResponse asyncResponse, @Context HttpServletRequest httpReq) {
    if (request == null) {
      throw new BadRequestException("Request must not be null");
    }
    String userId = getUserId();
    List<TopicDo> topics = request.getTopics();
    LOG.debug("Received request for topics {} and user {}", topics, userId);
    if (topics == null || topics.isEmpty()) {
      throw new BadRequestException("Topics must not be empty");
    }

    ClientDisconnectedListener clientDisconnectedListener = new ClientDisconnectedListener();
    httpReq.getAsyncContext().addListener(clientDisconnectedListener);

    getRegistry().getOrWait(topics, userId)
        .thenApply((notifications) -> {
          if (asyncResponse.isSuspended() && !clientDisconnectedListener.isDisconnected()) {
            logResponse(notifications, topics, userId);
            return asyncResponse.resume((new UiNotificationResponse().withNotifications(notifications)));
          }
          LOG.debug("Response is not available anymore, discarding {} notifications for topics {} and user {}", notifications.size(), topics, userId);
          return false;
        });
  }

  protected void logResponse(List<UiNotificationDo> notifications, List<TopicDo> topics, String userId) {
    if (notifications.isEmpty()) {
      if (LOG.isTraceEnabled()) {
        List<String> topicNames = topics.stream().map(topic -> topic.getName()).collect(Collectors.toList());
        LOG.trace("Resuming async response without any new notifications for topics {} and user {}", topicNames, userId);
      }
      return;
    }
    if (!LOG.isInfoEnabled()) {
      return;
    }

    String message = "Resuming async response: ";
    Map<Boolean, List<UiNotificationDo>> groups = notifications.stream().collect(Collectors.partitioningBy(notification -> notification.isSubscriptionStart()));
    List<UiNotificationDo> subscriptions = groups.get(true);
    if (subscriptions.size() > 0) {
      message += String.format("%s new subscription(s) for topics %s. ", subscriptions.size(), UiNotificationRegistry.getNotificationTopics(subscriptions));
    }
    notifications = groups.get(false);
    if (notifications.size() > 0) {
      message += String.format("%s new notification(s) for topics %s. ", notifications.size(), UiNotificationRegistry.getNotificationTopics(notifications));
    }
    LOG.info(message);
  }

  protected String getUserId() {
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }
}
