package org.eclipse.scout.rt.shared.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;

/**
 * <p>
 * Configuration property to define the default total maximum connections property for the Apache HTTP client.
 * </p>
 * <p>
 * The default setting of the Apache HTTP client itself would be 20. As the HTTP client is also used for the service
 * tunnel where more connections in parallel should exist, Scout sets the default to a much higher setting. This
 * property is used by the {@link ApacheHttpTransportFactory} but may be ignored if a custom
 * {@link IHttpTransportFactory} or a custom {@link HttpClientConnectionManager} is used.
 * </p>
 */
public class ApacheHttpTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

  @Override
  protected Integer getDefaultValue() {
    return 1024;
  }

  @Override
  public String getKey() {
    return "scout.http.apache_max_connections_total";
  }

}
