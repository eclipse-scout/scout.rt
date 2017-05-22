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

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.http.AbstractMultiSessionCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP cookie store implementation that manages different sets of cookies ("cookie jars"), one per {@link ISession}.
 */
@Bean
public class MultiSessionCookieStore extends AbstractMultiSessionCookieStore<CookieStore> implements CookieStore {

  private static final Logger LOG = LoggerFactory.getLogger(MultiSessionCookieStore.class);

  @Override
  protected CookieStore createDefaultCookieStore() {
    CookieStore cookieStore = createNewCookieStore();
    return new P_DefaultCookieStoreDecorator(cookieStore);
  }

  @Override
  protected CookieStore createNewCookieStore() {
    // Because java.net.InMemoryCookieStore is package private, this is the only way to create a new instance.
    return new CookieManager().getCookieStore();
  }

  @Override
  public void add(URI uri, HttpCookie cookie) {
    getDelegate().add(uri, cookie);
  }

  @Override
  public List<HttpCookie> get(URI uri) {
    return getDelegate().get(uri);
  }

  @Override
  public List<HttpCookie> getCookies() {
    return getDelegate().getCookies();
  }

  @Override
  public List<URI> getURIs() {
    return getDelegate().getURIs();
  }

  @Override
  public boolean remove(URI uri, HttpCookie cookie) {
    return getDelegate().remove(uri, cookie);
  }

  @Override
  public boolean removeAll() {
    return getDelegate().removeAll();
  }

  private static class P_DefaultCookieStoreDecorator implements CookieStore {

    private final CookieStore m_cookieStore;

    public P_DefaultCookieStoreDecorator(CookieStore cookieStore) {
      m_cookieStore = cookieStore;
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
      Assertions.assertNotNull(cookie);
      Exception e = null;
      if (LOG.isDebugEnabled()) {
        e = new Exception("stack trace for debugging");
      }
      LOG.warn("adding cookie to default cookie store which could be used by other users too [uri={}, cookieName={}]", uri, cookie.getName(), e);
      m_cookieStore.add(uri, cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
      return m_cookieStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
      return m_cookieStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
      return m_cookieStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
      return m_cookieStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
      return m_cookieStore.removeAll();
    }
  }
}
