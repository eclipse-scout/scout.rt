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
package org.eclipse.scout.rt.server.services.common.session;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

/**
 * Default implementation of {@link IServerSessionRegistryService}.
 */
public class ServerSessionRegistryService extends AbstractService implements IServerSessionRegistryService {

  @Override
  public <T extends IServerSession> T newServerSession(final Class<T> clazz, final Subject subject) throws ProcessingException {
    return newServerSession(clazz, subject, UserAgent.createDefault());
  }

  @Override
  public <T extends IServerSession> T newServerSession(final Class<T> clazz, final Subject subject, final UserAgent userAgent) throws ProcessingException {
    return newServerSession(clazz, ServerJobInput.defaults().subject(subject).userAgent(userAgent));
  }

  @Override
  public <T extends IServerSession> T newServerSession(final Class<T> clazz, final ServerJobInput input) throws ProcessingException {
    final T serverSession = newInstance(clazz);

    final Bundle bundle = Platform.getBundle(clazz.getPackage().getName());

    loadSessionInServerJob(input.copy().session(serverSession), bundle, serverSession);

    return serverSession;
  }

  /**
   * Method invoked to create an empty session instance of the given class.
   */
  @Internal
  protected <T extends IServerSession> T newInstance(final Class<T> clazz) throws ProcessingException {
    try {
      return clazz.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      throw new ProcessingException("Failed to instantiate server session: " + clazz, e);
    }
  }

  /**
   * Method invoked to load the server session on behalf of a server job.
   *
   * @param input
   *          input with the context to create a {@link IServerSession}.
   * @param bundle
   *          the bundle that contains the session class.
   * @param serverSession
   *          session to be loaded.
   * @throws ProcessingException
   */
  @Internal
  protected void loadSessionInServerJob(final ServerJobInput input, final Bundle bundle, final IServerSession serverSession) throws ProcessingException {
    IServerJobManager.DEFAULT.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        serverSession.loadSession(bundle);
      }
    }, input);
  }
}
