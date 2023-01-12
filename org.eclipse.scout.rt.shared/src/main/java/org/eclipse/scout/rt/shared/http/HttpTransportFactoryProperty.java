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

import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;

/**
 * <p>
 * Configuration property to define the default {@link IHttpTransportFactory}.
 * </p>
 * <p>
 * If property is not set, the default is {@link ApacheHttpTransportFactory}.
 * </p>
 */
public class HttpTransportFactoryProperty extends AbstractClassConfigProperty<IHttpTransportFactory> {

  @Override
  public Class<? extends IHttpTransportFactory> getDefaultValue() {
    return ApacheHttpTransportFactory.class;
  }

  @Override
  @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
  public String description() {
    return String.format("Fully qualified class name of the HTTP transport factory the application uses. The class must implement '%s'.\n"
        + "By default '%s' is used.", IHttpTransportFactory.class.getName(), ApacheHttpTransportFactory.class.getName());
  }

  @Override
  public String getKey() {
    return "scout.http.transportFactory";
  }
}
