/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared;

import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
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
   * when changed on the server.
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Returns true if the session has been loaded and is running.
   */
  boolean isActive();

  /**
   * @return <code>true</code> if session shutdown is in progress (i.g. {@link #stop()} was called). While shutting
   * down, the session is still considered "active".
   */
  boolean isStopping();

  Object getData(String key);

  void setData(String key, Object value);

  Object computeDataIfAbsent(String key, Callable<?> producer);

  IFastListenerList<ISessionListener> sessionListeners();

  /**
   * Registers the given listener to be notified about session state changes. Typically, a listener is installed in
   * <code>execLoadSession</code>.
   */
  default void addListener(ISessionListener sessionListener) {
    sessionListeners().add(sessionListener);
  }

  /**
   * Removes the given listener; has no effect if not registered.
   */
  default void removeListener(ISessionListener sessionListener) {
    sessionListeners().remove(sessionListener);
  }

  /**
   * Invoke this method to initialize the session. The session is active just after this method returns.
   *
   * @param sessionId
   *     unique id
   */
  void start(String sessionId);

  /**
   * Invoke this method to stop the session. This is the last call on the session.
   */
  void stop();
}
