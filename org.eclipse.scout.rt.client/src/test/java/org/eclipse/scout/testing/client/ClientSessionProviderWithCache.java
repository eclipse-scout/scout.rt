/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.LRUCache;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.testing.client.TestingClientConfigProperties.ClientSessionCacheExpirationProperty;

/**
 * Provider for client sessions. A client session is only created if not contained in the session cache.
 */
public class ClientSessionProviderWithCache extends ClientSessionProvider {

  private final LRUCache<CompositeObject, IClientSession> m_cache;

  public ClientSessionProviderWithCache() {
    m_cache = new LRUCache<>(1000, CONFIG.getPropertyValue(ClientSessionCacheExpirationProperty.class));
  }

  @Override
  public <SESSION extends IClientSession> SESSION provide(ClientRunContext runContext) throws ProcessingException {
    final Subject subject = Assertions.assertNotNull(runContext.subject(), "Subject must not be null");
    final Set<Principal> principals = subject.getPrincipals();
    Assertions.assertFalse(principals.isEmpty(), "Subject contains no principals");

    SESSION clientSession = getFromCache(principals, BEANS.get(IClientSession.class).getClass());
    if (clientSession != null) {
      return clientSession;
    }
    else {
      // create and initialize a new session; use optimistic locking because initializing the session is a long-running operation.
      final SESSION newClientSession = super.provide(runContext);

      synchronized (m_cache) {
        clientSession = getFromCache(principals, newClientSession.getClass()); // optimistic locking: check, whether another thread already created and cached the session.
        if (clientSession != null) {
          return clientSession;
        }
        else {
          return putToCache(principals, newClientSession);
        }
      }
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
    clientSession.getVirtualDesktop().addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        switch (e.getType()) {
          case DesktopEvent.TYPE_MESSAGE_BOX_ADDED:
            e.getMessageBox().getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
            break;
        }
      }
    });
  }

  @Internal
  protected <SESSION extends IClientSession> SESSION getFromCache(final Collection<Principal> principals, final Class<? extends IClientSession> clientSessionClass) throws ProcessingException {
    for (final Principal principal : principals) {
      final IClientSession clientSession = m_cache.get(newCacheKey(clientSessionClass, principal));
      if (clientSession != null) {
        return ClientSessionProvider.cast(clientSession);
      }
    }
    return null;
  }

  @Internal
  protected <SESSION extends IClientSession> SESSION putToCache(final Collection<Principal> principals, final SESSION clientSession) throws ProcessingException {
    for (final Principal principal : principals) {
      m_cache.put(newCacheKey(clientSession.getClass(), principal), clientSession);
    }
    return clientSession;
  }

  @Internal
  protected CompositeObject newCacheKey(final Class<? extends IClientSession> clientSessionClass, final Principal principal) {
    return new CompositeObject(clientSessionClass, principal);
  }
}
