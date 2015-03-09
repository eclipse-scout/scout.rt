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
package org.eclipse.scout.rt.server;

import java.util.Locale;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.osgi.framework.Bundle;

/**
 * Server-side session
 */
public interface IServerSession extends ISession {

  /**
   * Invoke this method to initialize the session. The session is active just after this method returns.
   */
  void loadSession() throws ProcessingException;

  @Deprecated
  void loadSession(Bundle bundle) throws ProcessingException;

  /**
   * Set the session id. Should only be done during initialization.
   *
   * @param id
   */
  void setIdInternal(String id);

  /**
   * @return a unique id
   */
  String getId();

  /**
   * @return {@link Locale} which is currently associated with the current thread, or the JVM default Locale if not set.
   * @deprecated use {@link NlsLocale#CURRENT} instead; will be removed in release 5.2<br/>
   *             reason: The Locale is tied very strong to the current executing job: if triggered by a
   *             client-request, the Locale is included in every request and set accordingly onto the job. Otherwise,
   *             the
   *             submitter of the job decides which Locale to use.
   */
  @Deprecated
  Locale getLocale();

  /**
   * @return {@link UserAgent} which is currently associated with the current thread, or
   *         {@link UserAgent#createDefault()} if not set.
   * @deprecated use {@link UserAgent#CURRENT} instead; will be removed in release 5.2<br/>
   *             reason: The UserAgent is tied very strong to the current executing job: if triggered by a
   *             client-request, the agent is included in every request and set accordingly onto the job. Otherwise, the
   *             submitter of the job decides which UserAgent to use, if applicable at all.
   */
  @Deprecated
  UserAgent getUserAgent();
}
