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

import java.util.UUID;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.IService;

@Priority(-3)
public interface IClientSessionRegistryService extends IService {

  /**
   * Creates and starts a new {@link IClientSession}. The session is started by the caller
   * thread.
   * </p>
   * <p>
   * Note: If the creation of the session requires a special jaas context call it only inside a
   * {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section.
   * </p>
   * <p>
   * Warning: Only use this method if the client environment is a rich client (swt, swing, ...) and therefore supports
   * singleton user sessions. Don't use it with web clients (rap, wicket, ...). With web clients rather use
   * {@link #newClientSession(Class, Subject, String, UserAgent)} and provide a virtualSessionId.
   * </p>
   * 
   * @param userAgent
   *          the current {@link UserAgent} which will be set on the client session.
   * @return a new client session of type clazz
   * @see {@link IClientSession#startSession(org.osgi.framework.Bundle)},{@link IClientSession#getUserAgent()},
   *      {@link IClientSession#isActive()}
   */
  <T extends IClientSession> T newClientSession(Class<T> clazz, UserAgent userAgent);

  /**
   * Returns the client session which was created and cached by {@link #newClientSession(Class, UserAgent)} or
   * {@link #newClientSession(Class, Subject, String, UserAgent)}.
   * 
   * @return the created client session for the given class or null if no client session for the given class could be
   *         found
   */
  <T extends IClientSession> T getClientSessionFor(Class<T> clazz);

  /**
   * <p>
   * A new session is created and started. The session is started by the caller thread.
   * </p>
   * <p>
   * Note: If the creation of the session requires a special jaas context call it only inside a
   * {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section.
   * </p>
   * 
   * @param virtualSessionId
   *          a unique id which will be set on the client session. Do not reuse an existing id. An id can be created by
   *          using {@link UUID#randomUUID()}.
   * @param userAgent
   *          the current {@link UserAgent} which will be set on the client session.
   * @return a new client session of type clazz
   * @see {@link IClientSession#startSession(org.osgi.framework.Bundle)}, {@link IClientSession#getVirtualSessionId()},
   *      {@link IClientSession#getUserAgent()},{@link IClientSession#isActive()}
   */
  <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId, UserAgent userAgent);

  /**
   * Creates and starts a new {@link IClientSession}. Such a new session is created and started once per osgi. It is
   * cached as long as it is active
   * <p>
   * Note: If the creation of the session requires a special jaas context call it only inside a
   * {@link Subject#doAs(Subject, java.security.PrivilegedAction)} section
   * <p>
   * Warning: Only use this method if the client environment is a rich client (swt, swing, ...) and therefore supports
   * singleton user sessions. Don't use it with web clients (rap, wicket, ...). With web clients rather use
   * {@link #newClientSession(Class, Subject, String, UserAgent)} and provide a virtualSessionId.
   * 
   * @return the cached (if active) client session of type clazz or a new one if
   *         none was cached
   *         <p>
   * @deprecated Use {@link #newClientSession(Class, UserAgent)}, {@link ClientJob#getCurrentSession()} or
   *             {@link #getClientSessionFor(Class)} instead.
   *             Will be removed in 3.9.0
   * @see {@link IClientSession#isActive()}, {@link IClientSession#startSession(org.osgi.framework.Bundle)}
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
