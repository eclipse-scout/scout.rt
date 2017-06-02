package org.eclipse.scout.rt.shared.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;

/**
 * <p>
 * Configuration property to define the default maximum life time in milliseconds for kept alive connections for the
 * Apache HTTP client.
 * </p>
 * <p>
 * The default setting of the Apache HTTP client itself would be indefinite. This property is used by the
 * {@link ApacheHttpTransportFactory} but may be ignored if a custom {@link IHttpTransportFactory} or a custom
 * {@link HttpClientConnectionManager} is used.
 * </p>
 */
public class ApacheHttpTransportConnectionTimeToLiveProperty extends AbstractIntegerConfigProperty {

  @Override
  protected Integer getDefaultValue() {
    return 1000 * 60 * 60; // default: 1 hour
  }

  @Override
  public String getKey() {
    return "scout.http.apache_connection_time_to_live";
  }

}
