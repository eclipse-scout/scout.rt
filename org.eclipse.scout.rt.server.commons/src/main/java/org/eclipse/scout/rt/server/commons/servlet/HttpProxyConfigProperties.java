/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.shared.http.async.AbstractAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager;

public final class HttpProxyConfigProperties {

  private HttpProxyConfigProperties() {
  }

  public static class HttpProxyAsyncHttpClientManagerConfigProperty extends AbstractClassConfigProperty<AbstractAsyncHttpClientManager> {

    @Override
    public String getKey() {
      return "scout.http.proxy.async.clientManager";
    }

    @Override
    public String description() {
      return "Default client manager for HttpProxy bean; users of this bean may choose a different client manager.";
    }

    @Override
    public Class<? extends AbstractAsyncHttpClientManager> getDefaultValue() {
      return DefaultAsyncHttpClientManager.class;
    }
  }

  public static class HttpProxyAsyncTimeoutConfigProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.http.proxy.async.timeout";
    }

    @Override
    public String description() {
      return "Timeout for async servlet contexts.";
    }

    @Override
    public Long getDefaultValue() {
      return TimeUnit.HOURS.toMillis(1);
    }
  }
}
