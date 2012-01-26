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

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.osgi.framework.Bundle;

public interface IClientSession {

  /**
   * Monitor can be used to wait for changes of the states 'active' and 'loaded'
   */
  Object getStateLock();

  /**
   * @returns the reference to the immutable shared variable map
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Shared context variable containing the authenticated userId in lowercase
   */
  @FormData
  String getUserId();

  /**
   * The session is running
   */
  boolean isActive();

  /**
   * The session is running and was loaded
   */
  boolean isLoaded();

  Throwable getLoadError();

  ScoutTexts getTexts();

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

  Bundle getBundle();

  IServiceTunnel getServiceTunnel();

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

  /**
   * @deprecated, use {@link #isSingleThreadSession()} instead
   */
  @Deprecated
  boolean isWebSession();

  /**
   * @return used to force immediate (in-thread) execution of client jobs, see {@link ClientJob#shouldSchedule()}
   *         <p>
   *         used in apache wicket type of scout apps, NOT in rwt/rap type apps
   */
  boolean isSingleThreadSession();

  /**
   * If {@link IClientSession#getVirtualSessionId()} is not null then it is sent as part of the wsse security header to
   * the
   * server.
   * <p>
   * This is necessary since the web-gui-servlet is itself a single servlet that is calling the server-servlet /process
   * with a single cookie and therefore a single http session. When this session is set, the ServiceTunnelServlet
   * /process recognizes this and is not associating the scout server session with the HttpSession but with a custom
   * cache associated with this ajax (remote) session id.
   * <p>
   * 
   * @return rap/rwt/ajax session id (this is a uuid) or null if app is not running as web app
   */
  String getVirtualSessionId();

  /**
   * see {@link #getVirtualSessionId()}
   */
  void setVirtualSessionId(String sessionId);

  /**
   * The {@link IServiceTunnel} used by {@link IClientSession#getServiceTunnel()} checks for the Subject
   * under which the session is running and creates a WSSE security element.
   * <p>
   * Consumers can query for the {@link Subject} of a {@link IClientSession}
   * <p>
   * Providers can set the {@link Subject} associated with a {@link IClientSession}
   */
  Subject getSubject();

  void setSubject(Subject subject);

  /**
   * @return
   */
  IIconLocator getIconLocator();

  Object getData(String key);

  void setData(String key, Object data);

  /**
   * @return Returns the session's locale.
   */
  Locale getLocale();

  /**
   * Sets the session's locale used for formatting values and for translating texts.
   * 
   * @param l
   */
  void setLocale(Locale l);
}
