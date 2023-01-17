/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.testing.client;

import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.session.ClientSessionStopHelper;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.testing.client.TestingClientConfigProperties.ClientSessionCacheExpirationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central point to obtain cached client sessions.
 * <p>
 * If a session is found in cache, this session is returned, or else a new session created via
 * {@link ClientSessionProvider}.
 * <p>
 * A session is identified by its <em>sessionId</em>, or if not specified its <em>userId</em>.
 *
 * @since 5.1
 */
public class ClientSessionProviderWithCache extends ClientSessionProvider {

  /**
   * A thread local that stores an (optional) strategy how to handle message boxes in unit tests.
   * <p>
   * When running a unit test, {@link #handleMessageBox(IMessageBox)} closes all message boxes by default with the
   * {@link IMessageBox#CANCEL_OPTION}. If this is insufficient, this thread local provides the means to return another
   * value.
   * </p>
   * <p>
   * For convenient ways how to use this, see the {@link BlockingTestUtility}.
   * </p>
   *
   * @since 8.0.0
   */
  public static final ThreadLocal<ToIntFunction<IMessageBox>> MESSAGE_BOX_HANDLER_STRATEGY = new ThreadLocal<>();

  private static final Logger LOG = LoggerFactory.getLogger(ClientSessionProviderWithCache.class);

  private final ConcurrentExpiringMap<CompositeObject, IClientSession> m_cache;

  public ClientSessionProviderWithCache() {
    m_cache = createSessionCache(CONFIG.getPropertyValue(ClientSessionCacheExpirationProperty.class));
  }

  /**
   * Returns the cached client session for the context's {@link Subject}. On cache miss, a new session with a random
   * <em>sessionId</em> is created via {@link ClientSessionProvider}.
   *
   * @param clientRunContext
   *          applied during session start, and to get the session's {@link Subject}.
   * @return session found in cache, or a new session on cache miss.
   * @throws RuntimeException
   *           if session creation failed.
   */
  @Override
  public <SESSION extends IClientSession> SESSION provide(final ClientRunContext clientRunContext) {
    return provide(null, clientRunContext);
  }

  /**
   * Returns the cached client session for the given <em>sessionId</em>, or the context's {@link Subject} if
   * <em>sessionId</em> is not specified. On cache miss, a new session is created via {@link ClientSessionProvider}.
   *
   * @param sessionId
   *          unique session ID to identify the cached session. If <code>null</code>, the context's {@link Subject} is
   *          used for identification. On cache miss, this <em>sessionId</em> is used to create a new session, or a
   *          random UUID if <code>null</code>.
   * @param clientRunContext
   *          applied during session start, and to get the session's {@link Subject}.
   * @return session found in cache, or a new session on cache miss.
   * @throws RuntimeException
   *           if session creation failed.
   */
  @Override
  public <SESSION extends IClientSession> SESSION provide(final String sessionId, final ClientRunContext clientRunContext) {
    // 1. Create session lookup key.
    final CompositeObject sessionCacheKey = newSessionCacheKey(sessionId, clientRunContext.getSubject());
    if (sessionCacheKey == null) {
      LOG.warn("Cannot identify cached client session because the cache key is undefined  [sessionId={}, subject={}]", sessionId, clientRunContext.getSubject());
      return super.provide(sessionId, clientRunContext);
    }

    // 2. Lookup session in the cache.
    @SuppressWarnings("unchecked")
    SESSION clientSession = (SESSION) m_cache.get(sessionCacheKey);
    if (clientSession != null && clientSession.isActive() && !clientSession.isStopping()) {
      return clientSession;
    }

    // 3. Cache miss (optimistic locking because session creation might be a long running operation)
    prepareSessionCreatingClientRunContext(clientRunContext);
    clientSession = super.provide(sessionId, clientRunContext);

    // 4. Cache the new client session, or return present session if created by another thread in the meantime (optimistic locking).
    @SuppressWarnings("unchecked")
    final SESSION cachedClientSession = (SESSION) m_cache.putIfAbsent(sessionCacheKey, clientSession);
    if (cachedClientSession != null) {
      stopSession(clientSession);
      clientSession = cachedClientSession;
    }

    return clientSession;
  }

  protected ConcurrentExpiringMap<CompositeObject, IClientSession> createSessionCache(final long ttl) {
    return new ConcurrentExpiringMap<>(ttl, TimeUnit.MILLISECONDS, 1_000) {
      @Override
      protected void execEntryEvicted(CompositeObject key, IClientSession session) {
        stopSession(session);
      }
    };
  }

  protected void stopSession(IClientSession session) {
    if (session != null && session.isActive()) {
      BEANS.get(ClientSessionStopHelper.class).scheduleStop(session, true, "session housekeeping");
    }
  }

  protected CompositeObject newSessionCacheKey(final String sessionId, final Subject subject) {
    // Test specific: Make session class part of the cache key.
    //                That is because JUnit tests can be configured to run with another session via {@link RunWithClientSession}.

    if (sessionId != null) {
      return new CompositeObject(BEANS.get(IClientSession.class).getClass(), sessionId);
    }
    else if (subject != null) {
      return new CompositeObject(BEANS.get(IClientSession.class).getClass(), BEANS.get(IAccessControlService.class).getUserId(subject));
    }
    else {
      return null;
    }
  }

  /**
   * Hook method for adapting the {@link ClientRunContext} that is used for creating the new {@link IClientSession}
   * instance.
   *
   * @since 7.0
   */
  protected void prepareSessionCreatingClientRunContext(final ClientRunContext clientRunContext) {
    clientRunContext.withProperty("url", "http://localhost:8082"); // simulates the URL used by the user's browser
  }

  @Override
  protected void registerSessionForNotifications(final IClientSession session, final String sessionId) {
    if (BEANS.get(IServiceTunnel.class).isActive()) {
      super.registerSessionForNotifications(session, sessionId);
    }
    else {
      LOG.warn("Failed to register session for notifications.");
    }
  }

  @Override
  protected void beforeStartSession(final IClientSession clientSession, final String sessionId) {
    // Adds a DesktopListener to automatically cancel all message boxes.
    ClientRunContexts.copyCurrent().getDesktop().addDesktopListener(e -> {
      switch (e.getType()) {
        case DesktopEvent.TYPE_MESSAGE_BOX_SHOW:
          handleMessageBox(e.getMessageBox());
          break;
      }
    });
  }

  /**
   * Closes the message box with value determined by the {@link #MESSAGE_BOX_HANDLER_STRATEGY}, or as a default with
   * {@link IMessageBox#CANCEL_OPTION}.
   */
  protected void handleMessageBox(IMessageBox messageBox) {
    final ToIntFunction<IMessageBox> strategy = MESSAGE_BOX_HANDLER_STRATEGY.get();
    int option = IMessageBox.CANCEL_OPTION;
    if (strategy != null) {
      option = strategy.applyAsInt(messageBox);
    }
    messageBox.getUIFacade().setResultFromUI(option);
  }

  @Override
  protected void afterStartSession(IClientSession clientSession) {
    final IDesktop desktop = clientSession.getDesktop();
    if (desktop != null) {
      desktop.getUIFacade().openFromUI();
      desktop.getUIFacade().fireGuiAttached();
    }
  }
}
