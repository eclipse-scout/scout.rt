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
package org.eclipse.scout.rt.client;

import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public interface IClientSession extends ISession, IPropertyObserver {

  String PROP_LOCALE = "locale";

  /**
   * @return the session's {@link Locale} or <code>null</code> if not set.
   */
  Locale getLocale();

  /**
   * Sets the given {@link Locale} to the session and {@link NlsLocale#CURRENT}.
   *
   * @param locale
   *          Locale to be set.
   */
  void setLocale(Locale locale);

  /**
   * @return the session's {@link UserAgent}; is never <code>null</code>; contains information about the UI.
   */
  UserAgent getUserAgent();

  /**
   * Sets the given {@link UserAgent} to the session and {@link UserAgent#CURRENT}.
   *
   * @param userAgent
   *          {@link UserAgent} to be set.
   */
  void setUserAgent(UserAgent userAgent);

  /**
   * Monitor can be used to wait for changes of the states 'active' and 'loaded'
   */
  Object getStateLock();

  /**
   * Invoke this method to initialize the session. The session is active just after this method returns.
   * 
   * @param sessionId
   *          TODO
   */
  void start(String sessionId) throws ProcessingException;

  /**
   * send a stop signal to the session event queue<br>
   * check {@link #isActive()} to wait until the queue has in fact closed
   */
  void stop();

  void stop(int exitCode);

  /**
   * @return <code>true</code> if session shutdown is in progress (i.g. {@link #stop()} was called). While
   *         shutting down, the session is still considered "active".
   */
  boolean isStopping();

  int getExitCode();

  /**
   * @return a virtual desktop model assiciated with this client session
   *         <p>
   *         Before a desktop is set using {@link #setDesktop(IDesktop)} this is a virtual desktop ONLY used to early
   *         register observers.<br>
   *         Once a desktop has been set, this is the same as calling {@link #getDesktop()}
   */
  IDesktop getVirtualDesktop();

  /**
   * @return the desktop model assiciated with this client session
   *         <p>
   *         Desktop is available only after {@link #start(String)} and
   */
  IDesktop getDesktop();

  /**
   * set the desktop model assiciated with this client session
   */
  void setDesktop(IDesktop a) throws ProcessingException;

//  IClientServiceTunnel getServiceTunnel();

  /**
   * see {@link IMemoryPolicy}
   */
  IMemoryPolicy getMemoryPolicy();

  /**
   * see {@link IMemoryPolicy}
   */
  void setMemoryPolicy(IMemoryPolicy p);

  /**
   * @return Subject used for offline operations
   */
  Subject getOfflineSubject();

  void goOffline() throws ProcessingException;
}
