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
import java.util.Map;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.osgi.framework.Bundle;

/**
 * The base implementation {@link org.eclipse.scout.rt.server.servlet.DeploymentServiceConfig} uses the
 * scout.xml file for setting deployment specific service properties
 */
public interface IServerSession {

  Bundle getBundle();

  /**
   * Shared context variable containing the authenticated userId in lowercase
   */
  @FormData
  String getUserId();

  NlsLocale getNlsLocale();

  void setNlsLocale(NlsLocale l);

  Locale getLocale();

  void setLocale(Locale l);

  DynamicNls getNlsTexts();

  Object getAttribute(Object key);

  void setAttribute(Object key, Object value);

  Map<String, Object> getSharedVariableMap();

  /**
   * The session is running in its event loop
   */
  boolean isActive();

  void loadSession(Bundle bundle) throws ProcessingException;

  /**
   * @return used to foce sync execution of server jobs
   */
  boolean isWebSession();
}
