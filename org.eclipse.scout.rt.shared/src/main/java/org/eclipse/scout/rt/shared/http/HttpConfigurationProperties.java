package org.eclipse.scout.rt.shared.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;

public final class HttpConfigurationProperties {

  private HttpConfigurationProperties() {
  }

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
  public static class ApacheHttpTransportConnectionTimeToLiveProperty extends AbstractIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return 1000 * 60 * 60; // default: 1 hour
    }

    @Override
    public String getKey() {
      return "scout.http.apache_connection_time_to_live";
    }

  }

  /**
   * <p>
   * Configuration property to define the default maximum connections per route property for the Apache HTTP client.
   * </p>
   * <p>
   * The default setting of the Apache HTTP client itself would be 2. As the HTTP client is also used for the service
   * tunnel where practically all routes are the same, Scout sets the default to a much higher setting. This property is
   * used by the {@link ApacheHttpTransportFactory} but may be ignored if a custom {@link IHttpTransportFactory} or a
   * custom {@link HttpClientConnectionManager} is used.
   * </p>
   */
  public static class ApacheHttpTransportMaxConnectionsPerRouteProperty extends AbstractIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return 32;
    }

    @Override
    public String getKey() {
      return "scout.http.apache_max_connections_per_route";
    }

  }

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
  public static class ApacheHttpTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return 128;
    }

    @Override
    public String getKey() {
      return "scout.http.apache_max_connections_total";
    }

  }

  /**
   * <p>
   * Configuration property to enable/disable keep-alive connections.
   * </p>
   * <p>
   * As default setting the <i>http.keepAlive</i> system property is used, if this property is not set <code>true</code>
   * is used as default (similar to java.net client implementation).
   * </p>
   */
  public static class ApacheHttpTransportKeepAliveProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      String prop = System.getProperty("http.keepAlive");
      return prop != null ? Boolean.valueOf(prop) : true;
    }

    @Override
    public String getKey() {
      return "scout.http.apache_keep_alive";
    }

  }

  /**
   * <p>
   * Configuration property to enable/disable one retry for non-idempotent <i>POST</i> requests.
   * </p>
   * <p>
   * As default setting the <i>sun.net.http.retryPost</i> system property is used, if this property is not set
   * <code>true</code> is used as default (similar to java.net client implementation).
   * </p>
   */
  public static class ApacheHttpTransportRetryPostProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      String prop = System.getProperty("sun.net.http.retryPost");
      return prop != null ? Boolean.valueOf(prop) : true;
    }

    @Override
    public String getKey() {
      return "scout.http.apache_retry_post";
    }

  }

}
