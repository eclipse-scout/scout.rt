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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Provider for server sessions.
 */
@ApplicationScoped
public class ServerSessionProvider {

  /**
   * Provides a new {@link IServerSession} for the {@link Subject} contained in the given {@link ServerJobInput}.
   *
   * @param input
   *          Input to run the server job which initializes the {@link IServerSession}.
   * @return {@link IServerSession} created; is never <code>null</code>.
   * @throws ProcessingException
   *           is thrown if the {@link IServerSession} could not be created or initialized.
   */
  public <T extends IServerSession> T provide(final ServerJobInput input) throws ProcessingException {
    // Create an empty session instance.
    final T serverSession = ServerSessionProvider.cast(OBJ.one(IServerSession.class));
    serverSession.setIdInternal(String.format("%s-%s", serverSession.getClass().getName(), UUID.randomUUID()));

    // Initialize the session.
    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        serverSession.loadSession();
      }
    }, input.copy().name("server-session-initialization").session(serverSession));

    return serverSession;
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
  protected static <SESSION extends IServerSession> SESSION cast(final IServerSession serverSession) throws ProcessingException {
    try {
      return (SESSION) serverSession;
    }
    catch (final ClassCastException e) {
      throw new ProcessingException(String.format("Wrong session class [actual=%s]", serverSession.getClass().getName()), e);
    }
  }
}
