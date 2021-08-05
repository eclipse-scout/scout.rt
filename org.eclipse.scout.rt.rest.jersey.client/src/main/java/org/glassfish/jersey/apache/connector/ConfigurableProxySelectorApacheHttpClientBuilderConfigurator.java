/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.glassfish.jersey.apache.connector;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;

/**
 * Apache http client builder interceptor registering Scout {@link ConfigurableProxySelector}.
 */
@Bean
public class ConfigurableProxySelectorApacheHttpClientBuilderConfigurator implements ApacheHttpClientBuilderConfigurator {

  @Override
  public HttpClientBuilder configure(HttpClientBuilder httpClientBuilder) {
    return httpClientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
  }
}
