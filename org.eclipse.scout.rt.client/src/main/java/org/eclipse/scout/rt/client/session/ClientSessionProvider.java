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
package org.eclipse.scout.rt.client.session;

import java.util.UUID;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.clientnotification.IClientSessionRegistry;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.session.Sessions;

/**
 * Central point to obtain client sessions.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ClientSessionProvider {

  /**
   * Creates and initializes a new {@link IClientSession} with data as specified by the given {@link ClientRunContext}.
   * <p>
   * For <em>sessionId</em>, a random UUID is used. To specify a <em>sessionId</em>, use
   * {@link #provide(String, ClientRunContext)} instead.
   *
   * @param clientRunContext
   *          applied during session start.
   * @return the new session, is not <code>null</code>.
   * @throws RuntimeException
   *           if session creation failed.
   */
  public <SESSION extends IClientSession> SESSION provide(final ClientRunContext clientRunContext) {
    return provide(null, clientRunContext);
  }

  /**
   * Creates and initializes a new {@link IClientSession} with data as specified by the given {@link ClientRunContext}.
   *
   * @param sessionId
   *          unique session ID, or <code>null</code> to use a random {@link UUID}.
   * @param clientRunContext
   *          applied during session start.
   * @return the new session, is not <code>null</code>.
   * @throws RuntimeException
   *           if session creation failed.
   */
  public <SESSION extends IClientSession> SESSION provide(final String sessionId, final ClientRunContext clientRunContext) {
    final String sid = sessionId != null ? sessionId : Sessions.randomSessionId();

    // Create the session with the given context applied.
    return clientRunContext.call(() -> {
      // 1. Create an empty session instance.
      @SuppressWarnings("unchecked")
      final SESSION session = (SESSION) BEANS.get(IClientSession.class);

      // 2. Enable this session to receive client notifications.
      registerSessionForNotifications(session, sid);

      // 3. Load the session in the model thread.
      return ModelJobs.schedule(() -> {
        beforeStartSession(session, sid);
        session.start(sid);
        afterStartSession(session);
        return session;
      }, ModelJobs.newInput(ClientRunContexts.copyCurrent()
          .withSession(session, true))
          .withName("Starting ClientSession [sessionId={}]", sid)
          .withExceptionHandling(null, false))
          .awaitDoneAndGet();
    });
  }

  /**
   * Registers the {@link IClientSession} to receive client notifications.
   */
  protected void registerSessionForNotifications(final IClientSession session, final String sessionId) {
    BEANS.get(IClientSessionRegistry.class).register(session, sessionId);
  }

  /**
   * Method invoked before the session is started.
   */
  protected void beforeStartSession(final IClientSession clientSession, final String sessionId) {
    // NOOP: The default implementation does nothing
  }

  /**
   * Method invoked after the session is started.
   */
  protected void afterStartSession(final IClientSession clientSession) {
    // NOOP: The default implementation does nothing
  }

  /**
   * Returns the {@link IClientSession} associated with the current thread, or <code>null</code> if not available, or if
   * not of the type {@link IClientSession}.
   */
  public static IClientSession currentSession() {
    return Sessions.currentSession(IClientSession.class);
  }

  /**
   * Returns the {@link IClientSession} associated with the current thread, or <code>null</code> if not available, or if
   * not of the expected type.
   */
  public static <SESSION extends IClientSession> SESSION currentSession(final Class<SESSION> type) {
    return Sessions.currentSession(type);
  }
}
