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
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install and uninstall {@link MultiSessionCookieStore}
 */
public class MultiSessionCookieStoreInstaller {
  private static final Logger LOG = LoggerFactory.getLogger(MultiSessionCookieStoreInstaller.class);

  private CookieHandler m_oldCookieHandler;
  private CookieHandler m_newCookieHandler;

  public void install() {
    CookieHandler cookieHandler = CookieHandler.getDefault();
    // Install MultiSessionCookieStore
    if (cookieHandler != null) {
      LOG.warn("Overriding pre-installed cookie handler: {}", cookieHandlerToString(cookieHandler));
    }
    m_oldCookieHandler = cookieHandler;
    m_newCookieHandler = new CookieManager(new MultiSessionCookieStore(), CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(m_newCookieHandler);
    LOG.info("Successfully installed {}", cookieHandlerToString(m_newCookieHandler));
  }

  public void uninstall() {
    if (m_newCookieHandler != null) {
      if (CookieHandler.getDefault() == m_newCookieHandler) {
        CookieHandler.setDefault(m_oldCookieHandler);
        LOG.info("Successfully uninstalled {}", cookieHandlerToString(m_newCookieHandler));
      }
      else {
        LOG.warn("Could not uninstall {}, because it was apparently replaced with {}", cookieHandlerToString(m_newCookieHandler), cookieHandlerToString(CookieHandler.getDefault()));
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
