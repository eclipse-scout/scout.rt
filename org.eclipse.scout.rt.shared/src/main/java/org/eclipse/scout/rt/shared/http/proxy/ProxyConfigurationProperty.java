/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http.proxy;

import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;

public class ProxyConfigurationProperty extends AbstractStringListConfigProperty {

  @Override
  public String getKey() {
    return "scout.http.proxyPatterns";
  }

  @Override
  @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
  public String description() {
    return String.format("Configure proxies for the '%s'. If an URI matches a pattern the corresponding proxy will be used.\n"
        + "By default no proxy is used.\n"
        + "The property value is of the format REGEXP_FOR_URI=PROXY_HOST:PROXY_PORT\n"
        + "Example:\n"
        + "scout.http.proxyPatterns[0]=.*\\.example.com(:\\d+)?=127.0.0.1:8888\n"
        + "scout.http.proxyPatterns[1]=.*\\.example.org(:\\d+)?=proxy.company.com", ConfigurableProxySelector.class.getSimpleName());
  }
}
