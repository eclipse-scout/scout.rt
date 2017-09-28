/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link HttpSessionMutex}</h3> Provides a mutex for {@link HttpSession}s to synchronize on.
 * <p>
 * This class should be registered as listener in the {@code web.xml} to create a mutex for each session.
 * <p>
 * This class should not be instanced by clients!
 */
public final class HttpSessionMutex implements HttpSessionListener {

  private static final Logger LOG = LoggerFactory.getLogger(HttpSessionMutex.class);

  public static final String SESSION_MUTEX_ATTRIBUTE_NAME = "scout.httpsession.mutex";

  /**
   * Initializes the given {@link HttpSession} with a session mutex.
   * <p>
   * This method can be called by registering this class as {@link HttpSessionListener} in the {@code web.xml}.
   * <p>
   * Please note that some containers in some special conditions do not call the session listener on newly created
   * sessions.
   *
   * @param httpSession
   *          The {@link HttpSession} to prepare. Must not be {@code null}.
   * @throws AssertionException
   *           if the given HTTP session is <code>null</code>.
   * @throws IllegalStateException
   *           if the given HTTP session is invalid.
   */
  private void initMutex(HttpSession httpSession) {
    // we do not synchronize here because we expect to be called by the listener only.
    assertNotNull(httpSession).setAttribute(SESSION_MUTEX_ATTRIBUTE_NAME, new Object());
    LOG.debug("Prepared new HTTP session {}", httpSession.getId());
  }

  /**
   * Gets the mutex for the given {@link HttpSession} to synchronize on.
   * <p>
   * Returns the session mutex object ({@link #SESSION_MUTEX_ATTRIBUTE_NAME}) if available. To make this mutex object
   * available register the class {@link HttpSessionMutex} as listener in the {@code web.xml}.
   * <p>
   * If no mutex object is available, the {@link HttpSession} itself is returned as mutex. This is valid for many cases
   * and servlet containers.
   *
   * @param httpSession
   *          The {@link HttpSession} for which the mutex object should be returned. Must not be {@code null}.
   * @return A mutex for the given session. Never returns {@code null}.
   * @throws AssertionException
   *           if the given HTTP session is <code>null</code>.
   * @throws IllegalStateException
   *           if the given HTTP session is invalid.
   * @see HttpSessionListener
   * @see <a href=
   *      "http://stackoverflow.com/questions/9802165/is-synchronization-within-an-httpsession-feasible">is-synchronization-within-an-httpsession-feasible<a>
   * @see <a href=
   *      "http://stackoverflow.com/questions/616601/is-httpsession-thread-safe-are-set-get-attribute-thread-safe-operations">is-httpsession-thread-safe-are-set-get-attribute-thread-safe-operations</a>
   * @see <a href=
   *      "https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/web/util/WebUtils.java">Spring
   *      Implementation of method getSessionMutex</a>
   */
  public static Object of(HttpSession httpSession) {
    Object mutex = assertNotNull(httpSession).getAttribute(SESSION_MUTEX_ATTRIBUTE_NAME);
    if (mutex != null) {
      return mutex;
    }
    LOG.info("Session without mutex: {}. Consider registering {} as listener in the web.xml", httpSession.getId(), HttpSessionMutex.class, new Exception("origin"));
    return httpSession;
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    initMutex(event.getSession());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    // ignore notifications about destroyed HTTP sessions.
  }
}
