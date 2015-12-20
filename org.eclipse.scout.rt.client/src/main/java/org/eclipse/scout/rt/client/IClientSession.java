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
package org.eclipse.scout.rt.client;

import java.net.URI;
import java.util.Locale;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.job.ISchedulingSemaphore;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
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
   * @return the public {@link URI} in the browser which hosts this application (typically ending in /index.html or
   *         similar)
   */
  URI getBrowserURI();

  /**
   * Monitor can be used to wait for changes of the states 'active' and 'loaded'
   */
  Object getStateLock();

  /**
   * send a stop signal to the session event queue<br>
   * check {@link #isActive()} to wait until the queue has in fact closed
   */
  @Override
  void stop();

  void stop(int exitCode);

  /**
   * @return <code>true</code> if session shutdown is in progress (i.g. {@link #stop()} was called). While shutting
   *         down, the session is still considered "active".
   */
  boolean isStopping();

  int getExitCode();

  /**
   * @return a virtual desktop model assiciated with this client session
   *         <p>
   *         Before a desktop is set using {@link #setDesktop(IDesktop)} this is a virtual desktop ONLY used to early
   *         register observers.<br>
   *         Once a desktop has been set, this is the same as calling {@link #getDesktop()}
   * @deprecated use {@link #getDesktopElseVirtualDesktop()}; will be removed in version 6.1.
   */
  @Deprecated
  IDesktop getVirtualDesktop();

  /**
   * @return the {@link IDesktop} associated with this {@link IClientSession}, or a virtual {@link IDesktop} with
   *         limited functionality if not set yet.
   */
  IDesktop getDesktopElseVirtualDesktop();

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
   * @return the desktop model associated with this client session
   *         <p>
   *         Desktop is available only after {@link #start(String)} and
   */
  IDesktop getDesktop();

  /**
   * Sets the desktop model associated with this client session.
   */
  void setDesktop(IDesktop desktop);

  /**
   * see {@link IMemoryPolicy}
   */
  IMemoryPolicy getMemoryPolicy();

  /**
   * see {@link IMemoryPolicy}
   */
  void setMemoryPolicy(IMemoryPolicy memoryPolicy);

  /**
   * @param newMap
   */
  void replaceSharedVariableMapInternal(SharedVariableMap newMap);

  /**
   * Returns the <em>one-permit</em> {@link ISchedulingSemaphore} to run model jobs of this session in sequence, meaning
   * that only one model job is active at any given time for this session.
   */
  ISchedulingSemaphore getModelJobSemaphore();
}
