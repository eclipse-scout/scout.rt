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
package org.eclipse.scout.rt.client.session;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Provider for client sessions.
 */
@ApplicationScoped
public class ClientSessionProvider {

  /**
   * Provides a new {@link IClientSession} for the {@link Subject} contained in the given {@link ClientJobInput}.
   *
   * @param input
   *          Input to run the client job which initializes the {@link IClientSession}.
   * @return {@link IClientSession} created; is never <code>null</code>.
   * @throws ProcessingException
   *           is thrown if the {@link IClientSession} could not be created or initialized.
   */
  public <T extends IClientSession> T provide(final ClientJobInput input) throws ProcessingException {
    // Create an empty session instance.
    ClientJobInput in = input.copy();
    final T clientSession = ClientSessionProvider.cast(OBJ.get(IClientSession.class));
    if (in.getUserAgent() != null) {
      clientSession.setUserAgent(in.getUserAgent());
    }
    if (in.getLocale() != null) {
      clientSession.setLocale(in.getLocale());
    }

    // Initialize the session.
    OBJ.get(IModelJobManager.class).schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        clientSession.startSession();
      }
    }, in.name("client-session-initialization").session(clientSession)).get();

    return clientSession;
  }

  /**
   * @return The {@link IClientSession} which is associated with the current thread, or <code>null</code> if not found.
   */
  public static final IClientSession currentSession() {
    final ISession session = ISession.CURRENT.get();
    return (IClientSession) (session instanceof IClientSession ? session : null);
  }

  /**
   * @return The {@link IClientSession} which is associated with the current thread, or <code>null</code> if not found
   *         or not of the expected type.
   */
  public static final <SESSION extends IClientSession> SESSION currentSession(final Class<SESSION> type) {
    final IClientSession clientSession = currentSession();
    if (clientSession == null) {
      return null;
    }

    try {
      return ClientSessionProvider.cast(clientSession);
    }
    catch (final ProcessingException e) {
      return null; // NOOP
    }
  }

  @Internal
  @SuppressWarnings("unchecked")
  protected static <SESSION extends IClientSession> SESSION cast(final IClientSession clientSession) throws ProcessingException {
    try {
      return (SESSION) clientSession;
    }
    catch (final ClassCastException e) {
      throw new ProcessingException(String.format("Wrong session class [actual=%s]", clientSession.getClass().getName()), e);
    }
  }
}
