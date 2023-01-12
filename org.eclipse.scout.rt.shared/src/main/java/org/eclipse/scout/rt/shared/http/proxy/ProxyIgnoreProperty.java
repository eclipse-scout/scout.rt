/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.proxy;

import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;

public class ProxyIgnoreProperty extends AbstractStringListConfigProperty {

  @Override
  public String getKey() {
    return "scout.http.ignoreProxyPatterns";
  }

  @Override
  public String description() {
    return "Configure the proxy ignore list for the ConfigurableProxySelector. If an URI matches the pattern no proxy connection is used.\n"
        + "By default no proxy is configured.\n"
        + "Example:\n"
        + "scout.http.ignoreProxyPatterns[0]=https?://localhost(?::\\d+)?(?:/.*)?\n"
        + "scout.http.ignoreProxyPatterns[1]=...";
  }
}
