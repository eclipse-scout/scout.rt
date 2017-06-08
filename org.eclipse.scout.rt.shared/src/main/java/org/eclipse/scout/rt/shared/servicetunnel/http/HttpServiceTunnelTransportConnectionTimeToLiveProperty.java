package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportConnectionTimeToLiveProperty;

/**
 * <p>
 * Configuration property to define the default maximum life time in milliseconds for kept alive connections for the
 * HTTP service tunnel (if the Apache HTTP client is used, overrides
 * {@link ApacheHttpTransportConnectionTimeToLiveProperty}).
 * </p>
 *
 * @see ApacheHttpTransportConnectionTimeToLiveProperty
 */
public class HttpServiceTunnelTransportConnectionTimeToLiveProperty extends AbstractIntegerConfigProperty {

  @Override
  protected Integer getDefaultValue() {
    return 1000 * 60 * 60; // default: 1 hour
  }

  @Override
  public String getKey() {
    return "org.eclipse.scout.rt.servicetunnel.apache_connection_time_to_live";
  }

}
