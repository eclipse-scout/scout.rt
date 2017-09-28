/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractMultiSessionCookieStore} implementation for Apache HTTP Client.
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

    }

    @Override
    public List<Cookie> getCookies() {
      return m_cookieStore.getCookies();
    }

    @Override
    public void clear() {
      m_cookieStore.clear();
    }

    @Override
    public boolean clearExpired(Date date) {
      return m_cookieStore.clearExpired(date);
    }
  }
}
