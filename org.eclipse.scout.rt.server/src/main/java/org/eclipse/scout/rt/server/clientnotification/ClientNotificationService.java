/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 *
 */
public class ClientNotificationService implements IClientNotificationService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationService.class);
  private ClientNotificationRegistry m_notificationRegistry;

  @PostConstruct
  protected void setup() {
    m_notificationRegistry = BEANS.get(ClientNotificationRegistry.class);
  }

  @Override
  public String getUserIdOfCurrentSession() {
    return Assertions.assertNotNull(ISession.CURRENT.get()).getUserId();
  }

  @Override
  public void registerSession(String notificationNodeId, String sessionId, String userId) {
    m_notificationRegistry.registerSession(notificationNodeId, sessionId, userId);
  }

  @Override
  public void unregisterSession(String notificationNodeId) {

  }

  public void destroy(String notificationNodeId) {

  }

  @Override
  public List<ClientNotificationMessage> getNotifications(String notificationNodeId) {
    // TODO[aho] to be configured
    return m_notificationRegistry.consume(notificationNodeId, 30, 10, TimeUnit.SECONDS);
  }

}
