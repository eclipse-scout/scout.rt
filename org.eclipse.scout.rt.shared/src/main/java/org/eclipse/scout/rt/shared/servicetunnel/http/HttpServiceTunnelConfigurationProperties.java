package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;

public final class HttpServiceTunnelConfigurationProperties {

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
    protected Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.servicetunnel.apache_max_connections_per_route";
    }

  }

  /**
   * <p>
   * Configuration property to define the default total maximum connections property for the HTTP service tunnel (if the
   * Apache HTTP client is used, overrides {@link ApacheHttpTransportMaxConnectionsTotalProperty}).
   * </p>
   *
   * @see ApacheHttpTransportMaxConnectionsTotalProperty
   */
  public static class HttpServiceTunnelTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return 2048;
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.servicetunnel.apache_max_connections_total";
    }

  }

}
