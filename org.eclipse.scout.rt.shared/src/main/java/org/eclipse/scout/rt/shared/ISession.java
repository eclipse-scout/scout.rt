/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.Bean;
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
   * @return the shared variable map. Shared variables are automatically updated on the client by client notifications
   *         when changed on the server.
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Returns true if the session has been loaded and is running.
   */
  boolean isActive();

  ScoutTexts getTexts();

  Object getData(String key);

  void setData(String key, Object value);

  Object computeDataIfAbsent(String key, Callable<?> producer);

  /**
   * Registers the given listener to be notified about session state changes. Typically, a listener is installed in
   * <code>execLoadSession</code>.
   */
  void addListener(ISessionListener sessionListener);

  /**
   * Removes the given listener; has no effect if not registered.
   */
  void removeListener(ISessionListener sessionListener);

  /**
   * Invoke this method to initialize the session. The session is active just after this method returns.
   *
   * @param sessionId
   *          unique id
   */
  void start(String sessionId);

  /**
   * Invoke this method to stop the session. This is the last call on the session.
   */
  void stop();

}
