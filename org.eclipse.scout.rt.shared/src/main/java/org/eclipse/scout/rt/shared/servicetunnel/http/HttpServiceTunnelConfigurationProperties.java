/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;

public final class HttpServiceTunnelConfigurationProperties {

  private HttpServiceTunnelConfigurationProperties() {
  }

  /**
   * <p>
   * Configuration property to define the default maximum connections per route property for the HTTP service tunnel (if
   * the Apache HTTP client is used, overrides {@link ApacheHttpTransportMaxConnectionsPerRouteProperty}).
   * </p>
   *
   * @see ApacheHttpTransportMaxConnectionsPerRouteProperty
   */
  public static class HttpServiceTunnelTransportMaxConnectionsPerRouteProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "scout.servicetunnel.maxConnectionsPerRoute";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the default maximum connections per route property for the HTTP service tunnel.\n"
          + "Overrides the value from '%s' for the service tunnel.\n"
          + "Default value is 2048.", BEANS.get(ApacheHttpTransportMaxConnectionsPerRouteProperty.class).getKey());
    }
  }

  public static class HttpServiceTunnelTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "scout.servicetunnel.maxConnectionsTotal";
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Specifies the default total maximum connections property for the HTTP service tunnel.\n"
              + "Overrides the value from '%s' for the service tunnel.\n"
              + "The default value is 2048.",
          BEANS.get(ApacheHttpTransportMaxConnectionsTotalProperty.class).getKey());
    }
  }
}
