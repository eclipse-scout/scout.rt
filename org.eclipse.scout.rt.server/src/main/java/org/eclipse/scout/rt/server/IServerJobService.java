/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * Service for {@link ServerJob}s that are not user specific, and run with backend-subject and backend-session on the
 * server.
 *
 * @since 4.2
 */
public interface IServerJobService extends IService {

  /**
   * @return a new {@link Subject} used for the backend session.
   */
  Subject getServerSubject();

  /**
   * @return a new {@link Subject} with given principal
   */
  Subject createSubject(String principal);

  /**
   * @return Class used for backendSessions
   * @throws ProcessingException
   *           if class not found or has wrong type
   */
  Class<? extends IServerSession> getServerSessionClass() throws ProcessingException;

  /**
   * @return a new session for backend jobs
   */
  IServerSession createServerSession() throws ProcessingException;

  /**
   * @return a new session for backend jobs for given class and subject
   */
  IServerSession createServerSession(Class<? extends IServerSession> sessionClazz, Subject subject) throws ProcessingException;

  /**
   * @return factory for creating server jobs with backend session and subject
   */
  IServerJobFactory createJobFactory() throws ProcessingException;

  /**
   * @return factory for creating server jobs with specific session and subject
   */
  IServerJobFactory createJobFactory(IServerSession session, Subject subject);

}
