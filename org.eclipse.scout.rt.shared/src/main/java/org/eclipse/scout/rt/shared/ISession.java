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
package org.eclipse.scout.rt.shared;

import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.session.ISessionListener;

/**
 * @since 3.8.0
 */
@Bean
public interface ISession {

  /**
   * The {@link ISession} which is currently associated with the current thread.
   */
  ThreadLocal<ISession> CURRENT = new ThreadLocal<>();

  /**
   * @return the session id corresponding client and server sessions do have the same id.
   */
  String getId();

  /**
   * Shared context variable containing the authenticated userId in lowercase
   */
  String getUserId();

  /**
   * @returns the reference to the immutable shared variable map
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Returns true if the session has been loaded and is running.
   */
  boolean isActive();

  ScoutTexts getTexts();

  Object getData(String key);

  void setData(String key, Object value);

  /**
   * Consumers can query for the {@link Subject} of a {@link IClientSession}
   * <p>
   * The {@link IServiceTunnel} used by {@link IClientSession#getServiceTunnel()} checks for the Subject under which the
   * session is running and creates a WSSE security element.
   * <p>
   * The subject is set when this object is created from {@link Subject#getSubject(java.security.AccessControlContext)}
   */
  Subject getSubject();

  void setSubject(Subject subject);

  /**
   * Registers the given listener to be notified about session state changes. Typically, a listener is installed in
   * <code>execLoadSession</code>.
   */
  void addListener(ISessionListener sessionListener);

  /**
   * Removes the given listener; has no effect if not registered.
   */
  void removeListener(ISessionListener sessionListener);

}
