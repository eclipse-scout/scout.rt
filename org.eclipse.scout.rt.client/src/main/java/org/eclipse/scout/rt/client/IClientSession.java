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
import java.util.Map;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.osgi.framework.Bundle;

public interface IClientSession extends ISession {

  /**
   * @return dedicated job manager to schedule model jobs on behalf of this {@link IClientServiceTunnel} in serial
   *         execution order.
   */
  IModelJobManager getModelJobManager();

  /**
   * @return the session's {@link Locale} or the JVM-default if unknown; is never <code>null</code>.
   */
  Locale getLocale();

  /**
   * To set the session's Locale. By default, this Locale will be included in every client-server-request unless the
   * client job is configured to run with another Locale.<br/>
   * By updating the session's Locale, the Locale of {@link NlsLocale#CURRENT} is updated as well.
   *
   * @param locale
   *          Locale to be set.
   */
  void setLocale(Locale locale);

  /**
   * @return {@link UserAgent} used; contains information about the UI; is never <code>null</code>.
   */
  UserAgent getUserAgent();

  /**
   * To set the user's {@link UserAgent}; contains information about the UI.
   *
   * @param userAgent
   *          {@link UserAgent} used;
   */
  void setUserAgent(UserAgent userAgent);

  /**
   * Monitor can be used to wait for changes of the states 'active' and 'loaded'
   */
  Object getStateLock();

  /**
   * The session is running and was loaded
   */
  boolean isLoaded();

  Throwable getLoadError();

  /**
   * Called before {@link #startSession(Bundle)} in order to prepare the state used to start the session with some
   * custom and runtime options
   *
   * @since 4.2
   */
  void initCustomParams(Map<String, String> customParams);

  /**
   * Start model thread with job queue<br>
   * The model thread will first load the session and then start the event loop
   * <p>
   * The session is active just after this method returns.
   */
  void startSession(Bundle bundle);

  /**
   * send a stop signal to the session event queue<br>
   * check {@link #isActive()} to wait until the queue has in fact closed
   */
  void stopSession();

  /**
   * @param exitCode
   *          {@link org.eclipse.equinox.app.IApplication#EXIT_OK},
   *          {@link org.eclipse.equinox.app.IApplication#EXIT_RELAUNCH},
   *          {@link org.eclipse.equinox.app.IApplication#EXIT_RESTART}
   */
  void stopSession(int exitCode);

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
   *         Desktop is available only after {@link #startSession(Bundle)} and
   *         <code>AbstractClientSession.execLoadSession()</code>
   */
  IDesktop getDesktop();

  /**
   * set the desktop model assiciated with this client session
   */
  void setDesktop(IDesktop a) throws ProcessingException;

  IClientServiceTunnel getServiceTunnel();

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

  void addLocaleListener(ILocaleListener listener);

  void removeLocaleListener(ILocaleListener listener);
}
