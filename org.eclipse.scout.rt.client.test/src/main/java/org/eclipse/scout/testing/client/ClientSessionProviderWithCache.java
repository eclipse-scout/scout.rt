/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.testing.client.TestingClientConfigProperties.ClientSessionCacheExpirationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for client sessions. A client session is only created if not contained in the session cache.
 */
public class ClientSessionProviderWithCache extends ClientSessionProvider {
  private static final Logger LOG = LoggerFactory.getLogger(ClientSessionProviderWithCache.class);

  private final ConcurrentMap<CompositeObject, IClientSession> m_cache;

  public ClientSessionProviderWithCache() {
    m_cache = createCacheMap();
  }

  protected ConcurrentMap<CompositeObject, IClientSession> createCacheMap() {
    long ttl = NumberUtility.nvl(CONFIG.getPropertyValue(ClientSessionCacheExpirationProperty.class), 0L);
    return new ConcurrentExpiringMap<CompositeObject, IClientSession>(ttl, TimeUnit.MILLISECONDS, 1000);
  }

  @Override
  public <SESSION extends IClientSession> SESSION provide(ClientRunContext runContext, String sessionId) {
    CompositeObject cacheKey = newCacheKey(runContext, sessionId);
    @SuppressWarnings("unchecked")
    SESSION clientSession = (SESSION) m_cache.get(cacheKey);
    if (clientSession != null) {
      return clientSession;
    }
    else {
      // create and initialize a new session; use optimistic locking because initializing the session is a long-running operation.
      clientSession = super.provide(runContext, sessionId);

      @SuppressWarnings("unchecked")
      SESSION oldClientSession = (SESSION) m_cache.putIfAbsent(cacheKey, clientSession);
      if (oldClientSession != null) {
        // optimistic locking: check, whether another thread already created and cached the session.
        return oldClientSession;
      }
      return clientSession;
    }
  }

  protected CompositeObject newCacheKey(ClientRunContext runContext, String sessionId) {
    Subject subject = Assertions.assertNotNull(runContext.getSubject(), "Subject must not be null");
    Class<? extends IClientSession> clientSessionClass = BEANS.get(IClientSession.class).getClass();
    String userId = BEANS.get(IAccessControlService.class).getUserId(subject);
    // if userId can not be determined, use sessionId as key and force therefore to create
    // and return a new session.
    return new CompositeObject(clientSessionClass, StringUtility.nvl(userId, sessionId));
  }

  @Override
  protected void registerClientSessionForNotifications(IClientSession session, String sessionId) {
    if (BEANS.get(IServiceTunnel.class).isActive()) {
      super.registerClientSessionForNotifications(session, sessionId);
    }
    else {
      LOG.warn("Error during session registration for notifications.");
    }
  }

  /**
   * Adds a {@link DesktopListener} that automatically cancels all message boxes.
   *
   * @param clientSession
   */
  @Override
  protected void beforeStartSession(IClientSession clientSession) {
    // auto-cancel all message boxes
    ClientRunContexts.copyCurrent().getDesktop().addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        switch (e.getType()) {
          case DesktopEvent.TYPE_MESSAGE_BOX_SHOW:
            e.getMessageBox().getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
            break;
        }
      }
    });
  }
}
