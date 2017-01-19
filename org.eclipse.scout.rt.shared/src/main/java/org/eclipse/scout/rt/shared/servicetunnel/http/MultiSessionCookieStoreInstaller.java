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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install and uninstall {@link MultiSessionCookieStore}
 */
@Bean
public class MultiSessionCookieStoreInstaller {
  private static final Logger LOG = LoggerFactory.getLogger(MultiSessionCookieStoreInstaller.class);

  private CookieHandler m_oldCookieHandler;
  private CookieHandler m_newCookieHandler;

  public void install() {
    CookieHandler cookieHandler = CookieHandler.getDefault();
    if (!checkMultiSessionCookieStoreAlreadyInstalled(cookieHandler)) {
      // Install MultiSessionCookieStore
      m_oldCookieHandler = cookieHandler;
      m_newCookieHandler = new CookieManager(BEANS.get(MultiSessionCookieStore.class), CookiePolicy.ACCEPT_ALL);
      CookieHandler.setDefault(m_newCookieHandler);
      LOG.info("Successfully installed {}", cookieHandlerToString(m_newCookieHandler));
    }
  }

  /**
   * MultiSessionCookieStore is registered within JRE for all instances of HttpClient, but cannot be shared between
   * multiple Scout deployments because the session lookup won't work between sessions and thread locals of different
   * class loaders. Detect if another deployment already installed a {@link MultiSessionCookieStore} and do not allow to
   * start the platform. Starting the platform without an installed {@link MultiSessionCookieStore} would cause that no
   * user can login to the application, if the application uses a client-server service tunnel (e.g. client and server
   * are deployed separately)
   *
   * @return {@code true} if a {@link MultiSessionCookieStore} was already installed by same deployment, else returns
   *         {@code false}.
   * @throws PlatformException
   *           if a {@link MultiSessionCookieStore} was already installed by another deployment
   */
  protected boolean checkMultiSessionCookieStoreAlreadyInstalled(CookieHandler cookieHandler) {
    if (cookieHandler != null) {
      if (cookieHandler instanceof CookieManager) {
        CookieStore cookieStore = ((CookieManager) cookieHandler).getCookieStore();
        if (cookieStore != null && cookieStore.getClass() == Platform.get().getBeanManager().getBean(MultiSessionCookieStore.class).getBeanClazz()) {
          LOG.info("Don't overriding already installed MultiSessionCookieStore cookie handler from same deployment: {}", cookieHandlerToString(cookieHandler));
          return true;
        }
        if (cookieStore != null && StringUtility.containsStringIgnoreCase(cookieStore.getClass().getSimpleName(), MultiSessionCookieStore.class.getSimpleName())) {
          throw new PlatformException("Instance of MultiSessionCookieStore already installed by another deployment. Only one Scout application using servlet tunnel is supported within one application server!");
        }
      }
      LOG.warn("Overriding pre-installed cookie handler: {}", cookieHandlerToString(cookieHandler));
    }
    return false;
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
