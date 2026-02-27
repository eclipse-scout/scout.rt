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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.shared.http.DefaultCookieStoreProvider.EnableDefaultCookieStoreProperty;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Test for {@link DefaultCookieStoreProvider}
 */
@RunWith(PlatformTestRunner.class)
public class DefaultCookieStoreProviderTest {

  @Rule
  public MockConfigPropertyRule<Boolean> m_enableDefaultCookieStoreConfigProperty = new MockConfigPropertyRule<>(EnableDefaultCookieStoreProperty.class, false);

  protected DefaultCookieStoreProvider m_provider = new DefaultCookieStoreProvider();

  @Test
  public void testDynamicAddCookie_disabled() {
    m_enableDefaultCookieStoreConfigProperty.setValue(false);
    CookieStore store = m_provider.provideDynamic();
    assertTrue(store.getCookies().isEmpty());

    Cookie cookie = Mockito.mock(Cookie.class);
    store.addCookie(cookie);
    assertTrue(store.getCookies().isEmpty());
  }

  @Test
  public void testDynamicAddCookie_enabled() {
    m_enableDefaultCookieStoreConfigProperty.setValue(true);
    CookieStore store = m_provider.provideDynamic();
    assertTrue(store.getCookies().isEmpty());

    Cookie cookie = Mockito.mock(Cookie.class);
    when(cookie.getName()).thenReturn("mock");
    store.addCookie(cookie);
    assertEquals(cookie, store.getCookies().getFirst());

    store.clear();
    assertTrue(store.getCookies().isEmpty());
  }

  @Test
  public void testCurrentSessionAddCookie_disabled() {
    m_enableDefaultCookieStoreConfigProperty.setValue(false);
    BasicCookieStore basicStore = new BasicCookieStore();
    CookieStore store = m_provider.provideCurrentSession(basicStore);
    assertTrue(store.getCookies().isEmpty());
    assertTrue(basicStore.getCookies().isEmpty());

    Cookie cookie = Mockito.mock(Cookie.class);
    store.addCookie(cookie);
    assertTrue(store.getCookies().isEmpty());
    assertTrue(basicStore.getCookies().isEmpty());
  }

  @Test
  public void testCurrentSessionAddCookie_enabled() {
    m_enableDefaultCookieStoreConfigProperty.setValue(true);
    BasicCookieStore basicStore = new BasicCookieStore();
    CookieStore store = m_provider.provideCurrentSession(basicStore);
    assertTrue(store.getCookies().isEmpty());
    assertTrue(basicStore.getCookies().isEmpty());

    Cookie cookie = Mockito.mock(Cookie.class);
    when(cookie.getName()).thenReturn("mock");
    store.addCookie(cookie);
    assertEquals(cookie, store.getCookies().getFirst());
    assertEquals(cookie, basicStore.getCookies().getFirst());

    store.clear();
    assertTrue(store.getCookies().isEmpty());
    assertTrue(basicStore.getCookies().isEmpty());
  }
}
