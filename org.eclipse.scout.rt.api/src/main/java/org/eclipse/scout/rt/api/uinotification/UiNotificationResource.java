/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.api.data.uinotification.TopicDo;
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
  public void get(UiNotificationRequest request, @Suspended AsyncResponse asyncResponse, @Context HttpServletRequest httpReq) {
    String userId = getUserId();
    List<TopicDo> topics = request.getTopics();
    LOG.debug("Received request for topics {} and user {}", topics, userId);

    ClientDisconnectedListener clientDisconnectedListener = new ClientDisconnectedListener();
    httpReq.getAsyncContext().addListener(clientDisconnectedListener);

    getRegistry().getOrWait(topics, userId)
        .thenApply((notifications) -> {
          if (asyncResponse.isSuspended() && !clientDisconnectedListener.isDisconnected()) {
            if (!notifications.isEmpty()) {
              LOG.info("Resuming async response with {} notifications for topics {} and user {}", notifications.size(), topics, userId);
            }
            else {
              LOG.debug("Resuming async response with {} notifications for topics {} and user {}", notifications.size(), topics, userId);
            }
            return asyncResponse.resume((new UiNotificationResponse().withNotifications(notifications)));
          }
          LOG.debug("Response is not available anymore, discarding {} notifications for topics {} and user {}", notifications.size(), topics, userId);
          return false;
        });
  }

  protected String getUserId() {
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }
}
