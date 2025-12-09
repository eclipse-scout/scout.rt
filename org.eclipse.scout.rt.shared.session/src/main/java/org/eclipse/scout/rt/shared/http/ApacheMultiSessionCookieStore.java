/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>{@link AbstractMultiSessionCookieStore} implementation for Apache HTTP Client.</p>
 * <p>This class is intended to be used only for synchronous requests as it recognizes the current session using a {@link ThreadLocal}. If requests are executed async (in another thread) sessions would not be respected.</p>
 */
public class ApacheMultiSessionCookieStore extends AbstractMultiSessionCookieStore<CookieStore> implements CookieStore {

  private static final Logger LOG = LoggerFactory.getLogger(ApacheMultiSessionCookieStore.class);

  @Override
  protected CookieStore createDefaultCookieStore() {
    CookieStore cookieStore = createNewCookieStore();
    return new P_DefaultCookieStoreDecorator(cookieStore);
  }

  @Override
  protected CookieStore createNewCookieStore() {
    return new BasicCookieStore();
  }

  @Override
  public void addCookie(Cookie cookie) {
    getDelegate().addCookie(cookie);
  }

  @Override
  public List<Cookie> getCookies() {
    return getDelegate().getCookies();
  }

  @SuppressWarnings("deprecation") // deprecated method must be implemented, required by interface
  @Override
  public boolean clearExpired(Date date) {
    return getDelegate().clearExpired(date);
  }

  @Override
  public void clear() {
    getDelegate().clear();
  }

  private static class P_DefaultCookieStoreDecorator implements CookieStore {

    private final CookieStore m_cookieStore;

    public P_DefaultCookieStoreDecorator(CookieStore cookieStore) {
      m_cookieStore = cookieStore;
    }

    @Override
    public void addCookie(Cookie cookie) {
      Assertions.assertNotNull(cookie);
      Exception e = null;
      if (LOG.isDebugEnabled()) {
        e = new Exception("stack trace for debugging");
      }
      LOG.warn("adding cookie to default cookie store which could be used by other users too [cookie: {}]", cookie, e);
      m_cookieStore.addCookie(cookie);
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
}
