/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link IMultiSessionCookieStoreProvider} rejecting cookies.
 *
 * @see EnableDefaultCookieStoreProperty to enable a default, session-less cookie store shared among all users.
 */
public class DefaultCookieStoreProvider implements IMultiSessionCookieStoreProvider {

  @Override
  public CookieStore provideDynamic() {
    return new P_DefaultCookieStoreDecorator(new BasicCookieStore());
  }

  @Override
  public CookieStore provideCurrentSession(CookieStore store) {
    return new P_DefaultCookieStoreDecorator(store);
  }

  /**
   * {@link CookieStore} implementation wrapping a default cookie store and ignoring cookies according to {@link EnableDefaultCookieStoreProperty}.
   */
  protected static class P_DefaultCookieStoreDecorator implements CookieStore {

    private static final Logger LOG = LoggerFactory.getLogger(P_DefaultCookieStoreDecorator.class);

    protected final CookieStore m_cookieStore;
    protected final boolean m_defaultCookieStoreEnabled;

    public P_DefaultCookieStoreDecorator(CookieStore store) {
      m_cookieStore = store;
      m_defaultCookieStoreEnabled = CONFIG.getPropertyValue(EnableDefaultCookieStoreProperty.class);
    }

    @Override
    public void addCookie(Cookie cookie) {
      assertNotNull(cookie);
      Exception e = null;
      if (LOG.isDebugEnabled()) {
        e = new Exception("stack trace for debugging");
      }
      if (m_defaultCookieStoreEnabled) {
        LOG.warn("adding cookie to default cookie store which could be used by other users too [cookie: {}]", cookie, e);
        m_cookieStore.addCookie(cookie);
      }
      else {
        LOG.warn("ignoring cookie which could be used by other users too [cookie: {}]", cookie, e);
      }
    }

    @Override
    public List<Cookie> getCookies() {
      return m_cookieStore.getCookies();
    }

    @Override
    public void clear() {
      m_cookieStore.clear();
    }

    @SuppressWarnings("deprecation") // deprecated method must be implemented, required by interface
    @Override
    public boolean clearExpired(Date date) {
      return m_cookieStore.clearExpired(date);
    }
  }

  /**
   * Property to enable the use of a default cookie store shared among all users.
   */
  public static class EnableDefaultCookieStoreProperty extends AbstractBooleanConfigProperty {

    @Override
    public Boolean getDefaultValue() {
      return false;
    }

    @Override
    public String description() {
      return "Enable default cookie store. The default value is disabled (= false)";
    }

    @Override
    public String getKey() {
      return "scout.http.enableDefaultCookieStore";
    }
  }
}
