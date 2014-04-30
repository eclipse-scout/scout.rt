/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Hashtable;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.MultiClientSessionCookieStore;
import org.eclipse.scout.rt.ui.rap.login.internal.InternalNetAuthenticator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.rt.ui.rap";

  private static final String CLIENT_LOG_LEVEL = "org.eclipse.rwt.clientLogLevel";
  private static final String ALL_CLIENT_LOG_LEVEL = "ALL";
  public static final String MULTI_CLIENT_SESSION_COOKIESTORE = "org.eclipse.scout.rt.multiClientSessionCookieStoreEnabled";

  private static Activator m_plugin;

  private ServiceRegistration m_netAuthRegistration;

  public Activator() {
    System.setProperty(CLIENT_LOG_LEVEL, ALL_CLIENT_LOG_LEVEL);
  }

  public static Activator getDefault() {
    return m_plugin;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_plugin = this;
    // register net authenticator ui
    Hashtable<String, Object> map = new Hashtable<String, Object>();
    map.put(Constants.SERVICE_RANKING, -1);
    m_netAuthRegistration = context.registerService(java.net.Authenticator.class.getName(), new InternalNetAuthenticator(), map);
    installCookieStore(context);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (m_netAuthRegistration != null) {
      m_netAuthRegistration.unregister();
      m_netAuthRegistration = null;
    }
    m_plugin = null;
    super.stop(context);
  }

  private void installCookieStore(BundleContext context) {
    boolean isMultiClientSessionCookieStoreEnabled = StringUtility.parseBoolean(context.getProperty(MULTI_CLIENT_SESSION_COOKIESTORE));
    if (isMultiClientSessionCookieStoreEnabled) {
      CookieHandler.setDefault(new CookieManager(new MultiClientSessionCookieStore(), CookiePolicy.ACCEPT_ALL));
    }
  }
}
