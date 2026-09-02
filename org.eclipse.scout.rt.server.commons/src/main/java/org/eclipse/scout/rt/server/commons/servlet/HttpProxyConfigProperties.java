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

import jakarta.servlet.AsyncContext;

import org.eclipse.scout.rt.platform.Platform;
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

  /**
   * Sets the timeout (in milliseconds) for the {@link AsyncContext} used by the {@link HttpProxy} when forwarding requests.
   * <p>
   * <b>Note:</b> Keep this value smaller or equals to {@link org.eclipse.scout.rt.app.ApplicationProperties.ScoutApplicationStreamIdleTimeoutProperty}
   * to ensure the async context can be closed before the underlying connection is terminated.
   *
   * @see AsyncContext#setTimeout(long)
   */
  public static class HttpProxyAsyncTimeoutConfigProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.http.proxy.async.timeout";
    }

    @Override
    public String description() {
      return "Timeout in milliseconds for async servlet contexts. Default: " + getDefaultValue() + ".";
    }

    @Override
    public Long getDefaultValue() {
      // Do not use a short timeout in DEV mode to allow longer debugging sessions
      return TimeUnit.MINUTES.toMillis(Platform.get().inDevelopmentMode() ? 60 : 6);
    }
  }
}
