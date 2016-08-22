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
package org.eclipse.scout.rt.server.session;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.session.Sessions;

/**
 * Central point to obtain server sessions.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ServerSessionProvider {

  /**
   * Creates and initializes a new {@link IServerSession} with data as specified by the given {@link ServerRunContext}.
   * <p>
   * For <em>sessionId</em>, a random UUID is generated. To specify a <em>sessionId</em>, use
   * {@link #provide(String, ServerRunContext)} instead.
   *
   * @param serverRunContext
   *          applied during session start.
   * @return the new session, is not <code>null</code>.
   * @throws RuntimeException
   *           if session creation failed.
   */
  public <SESSION extends IServerSession> SESSION provide(final ServerRunContext serverRunContext) {
    return provide(null, serverRunContext);
  }

  /**
   * Creates and initializes a new {@link IServerSession} with data as specified by the given {@link ServerRunContext}.
   *
   * @param sessionId
   *          unique session ID, or <code>null</code> to use a random {@link UUID}.
   * @param serverRunContext
   *          applied during session start.
   * @return the new session, is not <code>null</code>.
   * @throws RuntimeException
   *           if session creation failed.
   */
  public <SESSION extends IServerSession> SESSION provide(final String sessionId, final ServerRunContext serverRunContext) {
    final String sid = sessionId != null ? sessionId : Sessions.randomSessionId();

    // Create the session with the given context applied.
    return serverRunContext
        .copy()
        .withTransactionScope(TransactionScope.REQUIRES_NEW) // enforce a new transaction
        .call(new Callable<SESSION>() {

          @Override
          public SESSION call() throws Exception {
            // 1. Create an empty session instance.
            @SuppressWarnings("unchecked")
            final SESSION session = (SESSION) BEANS.get(IServerSession.class);

            // 2. Start the session.
            return ServerRunContexts.copyCurrent()
                .withSession(session)
                .withTransactionScope(TransactionScope.MANDATORY) // run in the same transaction
                .call(new Callable<SESSION>() {

              @Override
              public SESSION call() throws Exception {
                beforeStartSession(session, sid);
                session.start(sid);
                afterStartSession(session);
                return session;
              }
            });
          }
        });
  }

  /**
   * Method invoked before the session is started.
   */
  protected void beforeStartSession(final IServerSession serverSession, final String sessionId) {
    // NOOP: The default implementation does nothing
  }

  /**
   * Method invoked after the session is started.
   */
  protected void afterStartSession(final IServerSession serverSession) {
    // NOOP: The default implementation does nothing
  }

  /**
   * Returns the {@link IServerSession} associated with the current thread, or <code>null</code> if not available, or if
   * not of the type {@link IServerSession}.
   */
  public static final IServerSession currentSession() {
    return Sessions.currentSession(IServerSession.class);
  }

  /**
   * Returns the {@link IServerSession} associated with the current thread, or <code>null</code> if not available, or if
   * not of the expected type.
   */
  public static final <SESSION extends IServerSession> SESSION currentSession(final Class<SESSION> type) {
    return Sessions.currentSession(type);
  }
}
