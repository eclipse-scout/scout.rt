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

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStore;

@ApplicationScoped
public class HttpClientManager {

  public HttpClient getHttpClient() {
    Builder builder = HttpClient.newBuilder();

    installConfigurableProxySelector(builder);
    installMultiSessionCookieStore(builder);

    interceptBuildHttpClient(builder);
    return builder.build();
  }

  protected void installConfigurableProxySelector(Builder builder) {
    builder.proxy(BEANS.get(ConfigurableProxySelector.class));
    builder.followRedirects(Redirect.NORMAL);
  }

  protected void installMultiSessionCookieStore(Builder builder) {
    builder.cookieHandler(createCookieManager());
  }

  protected CookieManager createCookieManager() {
    return new CookieManager(BEANS.get(MultiSessionCookieStore.class), CookiePolicy.ACCEPT_ALL);
  }

  public void interceptBuildHttpClient(Builder builder) {
    // nop
  }
}
