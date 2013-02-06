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
package org.eclipse.scout.rt.shared;

import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.osgi.framework.Bundle;

/**
 * @since 3.8.0
 */
public interface ISession {

  Bundle getBundle();

  /**
   * Shared context variable containing the authenticated userId in lowercase
   */
  @FormData
  String getUserId();

  /**
   * @returns the reference to the immutable shared variable map
   */
  Map<String, Object> getSharedVariableMap();

  /**
   * Returns true if the session has been loaded and is running.
   */
  boolean isActive();

  ScoutTexts getTexts();

  /**
   * @return Returns the session's locale.
   */
  Locale getLocale();

  /**
   * Sets the session's locale used for formatting values and for translating texts.
   */
  void setLocale(Locale l);

  UserAgent getUserAgent();

  void setUserAgent(UserAgent userAgent);

  /**
   * Used to force immediate (in-thread) execution of jobs.
   */
  boolean isSingleThreadSession();

  Object getData(String key);

  void setData(String key, Object value);
}
