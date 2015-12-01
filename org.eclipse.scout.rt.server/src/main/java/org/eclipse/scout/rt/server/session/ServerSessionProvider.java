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
package org.eclipse.scout.rt.server.session;

import java.util.UUID;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Provider for server sessions.
 */
@ApplicationScoped
public class ServerSessionProvider {

  public <SESSION extends IServerSession> SESSION provide(final ServerRunContext serverRunContext) {
    return provide(serverRunContext, UUID.randomUUID().toString());
  }

  /**
   * Provides a new {@link IServerSession} for the {@link Subject} of the given {@link ServerRunContext}.
   *
   * @param serverRunContext
   *          <code>RunContext</code> initialized with the Subject used to create and load the session.
   * @return {@link IServerSession} created; is never <code>null</code>.
   * @throws ProcessingException
   *           is thrown if the {@link IServerSession} could not be created or initialized.
   */
  public <SESSION extends IServerSession> SESSION provide(final ServerRunContext serverRunContext, final String sessionId) {
    return serverRunContext.copy().call(new Callable<SESSION>() {

      @Override
      public SESSION call() throws Exception {
        // 1. Create an empty session instance.
        final SESSION serverSession = ServerSessionProvider.cast(BEANS.get(IServerSession.class));

        // 2. Load the session.
        ServerRunContexts.copyCurrent().withSession(serverSession).withTransactionScope(TransactionScope.MANDATORY).run(new IRunnable() {

          @Override
          public void run() throws Exception {
            serverSession.start(sessionId);
          }
        });

        return serverSession;
      }
    });
  }

  /**
   * @return The {@link IServerSession} which is associated with the current thread, or <code>null</code> if not found.
   */
  public static final IServerSession currentSession() {
    final ISession session = ISession.CURRENT.get();
    return (IServerSession) (session instanceof IServerSession ? session : null);
  }

  /**
   * @return The {@link IServerSession} which is associated with the current thread, or <code>null</code> if not found
   *         or not of the expected type.
   */
  public static final <SESSION extends IServerSession> SESSION currentSession(final Class<SESSION> type) {
    final IServerSession serverSession = currentSession();
    if (serverSession == null) {
      return null;
    }

    try {
      return ServerSessionProvider.cast(serverSession);
    }
    catch (final ProcessingException e) {
      return null; // NOOP
    }
  }

  @Internal
  @SuppressWarnings("unchecked")
  protected static <SESSION extends IServerSession> SESSION cast(final IServerSession serverSession) {
    try {
      return (SESSION) serverSession;
    }
    catch (final ClassCastException e) {
      throw new ProcessingException(String.format("Wrong session class [actual=%s]", serverSession.getClass().getName()), e);
    }
  }
}
