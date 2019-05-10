/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

import java.net.SocketException;

import org.apache.http.NoHttpResponseException;
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
   * This property is not used anymore.
   * <p>
   * https://git.eclipse.org/r/#/c/131382/
   * <p>
   * https://git.eclipse.org/r/#/c/131452/
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

  /**
   * Enable retry of request (includes non-idempotent requests) on {@link NoHttpResponseException}
   * <p>
   * Assuming that the cause of the exception was most probably a stale socket channel on the server side.
   * <p>
   * For apache tomcat see http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
   *
   * @since 7.0
   */
  public static class ApacheHttpTransportRetryOnNoHttpResponseExceptionProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String getKey() {
      return "scout.http.retryOnNoHttpResponseException";
    }
  }

  /**
   * Enable retry of request (includes non-idempotent requests) on {@link SocketException} with message "Connection
   * reset"
   * <p>
   * Assuming that the cause of the exception was most probably a stale socket channel on the server side.
   * <p>
   * For apache tomcat see http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
   *
   * @since 7.0
   */
  public static class ApacheHttpTransportRetryOnSocketExceptionByConnectionResetProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String getKey() {
      return "scout.http.retryOnSocketExceptionByConnectionReset";
    }
  }

  /**
   * Enable redirect of POST requests (includes non-idempotent requests). Default is true.
   *
   * @since 7.0
   */
  public static class ApacheHttpTransportRedirectPostProperty extends AbstractBooleanConfigProperty {

    @Override
    protected Boolean getDefaultValue() {
      return true;
    }

    @Override
    public String getKey() {
      return "scout.http.redirectPost";
    }
  }
}
