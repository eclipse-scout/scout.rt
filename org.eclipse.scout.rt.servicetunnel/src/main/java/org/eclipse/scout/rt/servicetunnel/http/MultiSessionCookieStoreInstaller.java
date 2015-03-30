/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.servicetunnel.http;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Install and uninstall {@link MultiSessionCookieStore}
 */
public class MultiSessionCookieStoreInstaller {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MultiSessionCookieStoreInstaller.class);

  private CookieHandler m_newCookieHandler;

  public void install() {
    CookieHandler cookieHandler = CookieHandler.getDefault();
    if (cookieHandler != null) {
      LOG.warn("Overriding pre-installed cookie handler: " + cookieHandlerToString(cookieHandler));
    }
    m_newCookieHandler = new CookieManager(new MultiSessionCookieStore(), CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(m_newCookieHandler);
    LOG.info("Successfully installed " + cookieHandlerToString(m_newCookieHandler));
  }

  public void uninstall() {
    if (m_newCookieHandler != null) {
      if (CookieHandler.getDefault() == m_newCookieHandler) {
        CookieHandler.setDefault(null);
        LOG.info("Successfully uninstalled " + cookieHandlerToString(m_newCookieHandler));
      }
      else {
        LOG.warn("Could not uninstall " + cookieHandlerToString(m_newCookieHandler) + ", because it was apparently replaced with " + cookieHandlerToString(CookieHandler.getDefault()));
      }
      m_newCookieHandler = null;
    }
  }

  protected String cookieHandlerToString(CookieHandler cookieHandler) {
    if (cookieHandler == null) {
      return "null";
    }
    String s = cookieHandler.toString();
    if (cookieHandler instanceof CookieManager) {
      CookieStore cookieStore = ((CookieManager) cookieHandler).getCookieStore();
      s += " (Cookie store: " + (cookieStore == null ? "null" : cookieStore.toString()) + ")";
    }
    return s;
  }
}
