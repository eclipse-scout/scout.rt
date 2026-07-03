/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.shared.session.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public interface IClientSession extends ISession, IPropertyObserver {

  String PROP_LOCALE = "locale";

  /**
   * Property for the shared variables. Events are fired for this property when the {@link #getSharedVariableMap()} has
   * changed.
   */
  String PROP_SHARED_VARIABLE_MAP = "sharedVariableMap";

  /**
   * @return the session's {@link Locale} or <code>null</code> if not set.
   */
  Locale getLocale();

  /**
   * Sets the given {@link Locale} to the session and {@link NlsLocale#CURRENT}.
   *
   * @param locale
   *     Locale to be set.
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
   *     {@link UserAgent} to be set.
   */
  void setUserAgent(UserAgent userAgent);

  /**
   * @return the public {@link URI} in the browser which hosts this application (typically ending in /index.html or
   * similar)
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

  int getExitCode();

  /**
   * @return the {@link IDesktop} associated with this {@link IClientSession}, or a virtual {@link IDesktop} with
   * limited functionality if not set yet.
   */
  IDesktop getDesktopElseVirtualDesktop();

  /**
   * Consumers can query for the {@link Subject} of a {@link IClientSession}
   * <p>
   * The subject is set when this object is created from {@link Subject#current()}
   */
  Subject getSubject();

  /**
   * Consumers can query for the {@link User} of a {@link IClientSession}
   * <p>
   * The user is set when this object is created from {@link User#current()}
   */
  User getUser();

  /**
   * Updates {@link Subject} and {@link User} on the {@link IClientSession} with given {@link RunContext}
   *
   * @deprecated this method is meant for internal framework usage only. Other usages are highly discouraged
   */
  @Deprecated
  void applySubjectAndUser(RunContext runContext);

  /**
   * Authenticated userId, provided by {@link User#getUserId()}}
   */
  String getUserId();

  /**
   * @return the desktop model associated with this client session
   * <p>
   * Desktop is available only after {@link #start(String)} and
   */
  IDesktop getDesktop();

  /**
   * Sets the desktop model associated with this client session.
   *
   * @param desktop
   *     Must not be null.
   * @throws IllegalStateException
   *     if this session already has a desktop set
   * @throws IllegalArgumentException
   *     if the given desktop is null
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
   * Returns the <em>one-permit</em> {@link IExecutionSemaphore} to run model jobs of this session in sequence, meaning
   * that only one model job is active at any given time for this session.
   */
  IExecutionSemaphore getModelJobSemaphore();

  /**
   * @return An unmodifiable {@link Set} with all property names of the {@link #getSharedVariableMap() shared variable
   * map} that should be accessible in the browser. Never returns {@code null}.
   */
  Set<String> getExposedSharedVariables();

  /**
   * @return the variable map holding additional information used by the client session
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Sets multiple shared variables and fires a change event. Existing entries with the same keys will be overwritten.
   *
   * @param variables
   *     map of variables that should be set
   */
  void setSharedVariables(Map<String, Object> variables);
}
