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
package org.eclipse.scout.rt.client.services.common.session;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.IService;

@Priority(-3)
public interface IClientSessionRegistryService extends IService {

  /**
   * @param clazz
   * @return the cached (if active) client session of type clazz or a new one if
   *         none was cached
   *         <p>
   *         a new such session is created and started once per osgi. It is cached as long as it is active. see
   *         {@link IClientSession#isActive()} and {@link IClientSession#startSession(org.osgi.framework.Bundle)}
   *         <p>
   *         Note: If the creation of the session requires a special jaas context call it only inside a
   *         {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section
   *         <p>
   *         Warning: only use this method if the client environment is swt, swing, ... and therefore supports singleton
   *         user sessions. Don't use it with web apps (rap, rwt, wicket)
   */
  <T extends IClientSession> T newClientSession(Class<T> clazz, UserAgent userAgent);

  /**
   * @param clazz
   * @return a new client session of type clazz
   *         <p>
   *         A new session is created and started. see {@link IClientSession#isActive()} and
   *         {@link IClientSession#startSession(org.osgi.framework.Bundle)}
   *         <p>
   *         Note: If the creation of the session requires a special jaas context call it only inside a
   *         {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section
   */
  <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId, UserAgent userAgent);

  /**
   * @deprecated Use {@link #newClientSession(Class, UserAgent)} or {@link ISessionService#getCurrentSession()} instead.<br/>
   *             Will be removed in 3.9.0
   */
  @Deprecated
  <T extends IClientSession> T getClientSession(Class<T> clazz);

  /**
   * @deprecated Use {@link #newClientSession(Class, Subject, String, UserAgent)} instead.<br/>
   *             Will be removed in 3.9.0
   */
  @Deprecated
  <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId);

}
