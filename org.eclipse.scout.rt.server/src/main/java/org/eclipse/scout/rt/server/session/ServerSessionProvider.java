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
    final T serverSession = cast(OBJ.one(IServerSession.class));
    serverSession.setIdInternal(String.format("%s-%s", serverSession.getClass().getName(), UUID.randomUUID()));

    // Initialize the session.
    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        serverSession.loadSession();
      }
    }, input.copy().session(serverSession));

    return serverSession;
  }

  @Internal
  @SuppressWarnings("unchecked")
  protected <T extends IServerSession> T cast(final IServerSession serverSession) throws ProcessingException {
    try {
      return (T) serverSession;
    }
    catch (final ClassCastException e) {
      throw new ProcessingException(String.format("Wrong session class [actual=%s]", serverSession.getClass().getName()), e);
    }
  }
}
