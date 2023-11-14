/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.shared.http.async.AbstractAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager;

public class HttpProxyAsyncHttpClientManagerConfigProperty extends AbstractClassConfigProperty<AbstractAsyncHttpClientManager> {

  @Override
  public String getKey() {
    return "scout.http.proxy.async.clientManager";
  }

  @Override
  public String description() {
    return "Define the default client manager to be used by the HttpProxy bean; users of this bean may however still overwrite the value.";
  }

  @Override
  public Class<? extends AbstractAsyncHttpClientManager> getDefaultValue() {
    return DefaultAsyncHttpClientManager.class;
  }
}
