/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.proxy;

import java.net.Proxy;

import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;

public class ProxyConfigurationProperty extends AbstractStringListConfigProperty {

  @Override
  public String getKey() {
    return "scout.http.proxyPatterns";
  }

  @Override
  @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
  public String description() {
    return String.format("""
            Configure proxies for the '%s'. If an URI matches a pattern the corresponding proxy will be used.
            By default no proxy is used.
            The property value is of the format REGEXP_FOR_URI=PROXY_HOST:PROXY_PORT
            Alternatively also a proxy type can be defined (must be a type of the enum %s) using this format REGEXP_FOR_URI=PROXY_TYPE=PROXY_HOST:PROXY_PORT
            Example:
            scout.http.proxyPatterns[0]=.*\\\\.example\\\\.com(:\\\\d+)?=127.0.0.1:8888
            scout.http.proxyPatterns[1]=.*\\\\.example\\\\.org(:\\\\d+)?=proxy.example.com:8080
            scout.http.proxyPatterns[2]=.*\\\\.example\\\\.net(:\\\\d+)?=SOCKS=proxy.example.com:1080""",
        ConfigurableProxySelector.class.getSimpleName(),
        Proxy.Type.class.getName());
  }
}
