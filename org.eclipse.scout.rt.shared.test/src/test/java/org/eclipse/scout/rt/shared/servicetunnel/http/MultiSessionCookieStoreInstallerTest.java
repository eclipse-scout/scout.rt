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
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link MultiSessionCookieStoreInstaller}
 */
public class MultiSessionCookieStoreInstallerTest {

  protected static class P_MyMultiSessionCookieStoreEx implements CookieStore {

    @Override
    public void add(URI uri, HttpCookie cookie) {
    }

    @Override
    public List<HttpCookie> get(URI uri) {
      return null;
    }

    @Override
    public List<HttpCookie> getCookies() {
      return null;
    }

    @Override
    public List<URI> getURIs() {
      return null;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
      return false;
    }

    @Override
    public boolean removeAll() {
      return false;
    }
  }

  protected static MultiSessionCookieStoreInstaller s_installer = new MultiSessionCookieStoreInstaller();

  @BeforeClass
  public static void beforeClass() {
    s_installer.install();
  }

  @AfterClass
  public static void afterClass() {
    s_installer.uninstall();
  }

  @Test
  public void checkMultiSessionCookieStoreAlreadyInstalled() {
    // allow to re-install same MultiSessionCookieStore class by same deployment
    s_installer.install();
  }

  @Test(expected = PlatformException.class)
  public void checkOtherMultiSessionCookieStoreAlreadyInstalled() {
    CookieManager cookieManager = Mockito.mock(CookieManager.class);
    CookieStore store = new P_MyMultiSessionCookieStoreEx();
    Mockito.when(cookieManager.getCookieStore()).thenReturn(store);
    s_installer.checkMultiSessionCookieStoreAlreadyInstalled(cookieManager);
  }

  @Test
  public void checkOtherCookieStoreAlreadyInstalled() {
    // Other CookieHandler implementation is installed -> OK
    CookieHandler cookieHandler = Mockito.mock(CookieHandler.class);
    s_installer.checkMultiSessionCookieStoreAlreadyInstalled(cookieHandler);

    // Other CookieManager implementation is installed -> OK
    CookieManager cookieManager = Mockito.mock(CookieManager.class);
    s_installer.checkMultiSessionCookieStoreAlreadyInstalled(cookieManager);

    // Other CookieManager with other CookieStore implementation is installed -> OK
    Mockito.when(cookieManager.getCookieStore()).thenReturn(Mockito.mock(CookieStore.class));
    s_installer.checkMultiSessionCookieStoreAlreadyInstalled(cookieManager);
  }
}
