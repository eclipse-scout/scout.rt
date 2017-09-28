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
package org.eclipse.scout.rt.shared.http.proxy;

import java.net.Proxy;
import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.config.AbstractStringListConfigProperty;

/**
 * <p>
 * Configure the proxy ignore list for the {@link ConfigurableProxySelector}. If an {@link URI} matches the
 * {@link Pattern} defined by this property a {@link Proxy#NO_PROXY} connection should be used.
 * </p>
 * <p>
 * This property is a list property, example valid configurations:
 * </p>
 *
 * <pre>
 * scout.http.ignore_proxy[0]=https?://localhost(?::\\d+)?(?:/.*)?
 * </pre>
 */
public class ProxyIgnoreProperty extends AbstractStringListConfigProperty {

  @Override
  public String getKey() {
    return "scout.http.ignore_proxy";
  }

}
