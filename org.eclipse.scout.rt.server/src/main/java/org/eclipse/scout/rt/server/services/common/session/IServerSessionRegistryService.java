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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.IService;

/**
 * Service to create and load new server sessions.
 */
public interface IServerSessionRegistryService extends IService {

  /**
   * Use this method to create and load a new server session.
   *
   * @param clazz
   *          class of the server session to be created.
   * @param subject
   *          if loading the session requires a specific <code>JAAS</code> context; normally, that is not the case; if
   *          set, the session is loaded within {@link Subject#doAs(Subject, java.security.PrivilegedAction)}.
   * @return {@link IServerSession}; is never <code>null</code>.
   * @throws ProcessingException
   *           if the session could not be created.
   * @see IServerSession#loadSession(org.osgi.framework.Bundle)
   */
  <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject) throws ProcessingException;

  /**
   * Use this method to create and load a new server session.
   *
   * @param clazz
   *          class of the server session to be created.
   * @param subject
   *          if loading the session requires a specific <code>JAAS</code> context; normally, that is not the case; if
   *          set, the session is loaded within {@link Subject#doAs(Subject, java.security.PrivilegedAction)}.
   * @param userAgent
   *          is set onto the context while loading the session.
   * @return {@link IServerSession}; is never <code>null</code>.
   * @throws ProcessingException
   *           if the session could not be created.
   * @see IServerSession#loadSession(org.osgi.framework.Bundle)
   */
  <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject, UserAgent userAgent) throws ProcessingException;

  /**
   * Use this method to create and load a new server session.
   *
   * @param clazz
   *          class of the server session to be created.
   * @param input
   *          {@link ServerJobInput} used to run the job on behalf of which the server session is loaded.
   * @return {@link IServerSession}; is never <code>null</code>.
   * @throws ProcessingException
   *           if the session could not be created.
   * @since 5.1
   * @see IServerSession#loadSession(org.osgi.framework.Bundle)
   */
  <T extends IServerSession> T newServerSession(Class<T> clazz, final ServerJobInput input) throws ProcessingException;
}
