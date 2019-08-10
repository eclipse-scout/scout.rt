/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
