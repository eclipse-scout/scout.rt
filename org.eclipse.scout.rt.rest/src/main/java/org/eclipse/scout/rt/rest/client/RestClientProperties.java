/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import java.util.logging.Level;

import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;

public final class RestClientProperties {

  /**
   * A value of {@code true} enables cookies.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * </p>
   * <p>
   * The default value is {@code false}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String ENABLE_COOKIES = "scout.rest.client.enableCookies";

  /**
   * Standard cookie specification used by underlying http client. See {@code org.apache.hc.client5.http.cookie.StandardCookieSpec}
   * for details.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.String}.
   * </p>
   * <p>
   * The default value is {@code "relaxed"}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String COOKIE_SPEC = "scout.rest.client.cookieSpec";

  /**
   * A value of {@code true} disables chunked transfer encoding.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * <p>
   * The default value is {@code false}.
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String DISABLE_CHUNKED_TRANSFER_ENCODING = "scout.rest.client.disableChunkedTransferEncoding";

  /**
   * A value of {@code true} ensures that the header 'Connection: close' is added to every REST HTTP request where the
   * 'Connection' header is not already set.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * <p>
   * The default value is {@code true}.
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String CONNECTION_CLOSE = "scout.rest.client.connectionClose";

  /**
   * Name used for REST client request/response logger.
   * <p>
   * Note: REST client request response logger is activated if at lease one of the LOGGING_LOGGER_* properties is set.
   */
  public static final String LOGGING_LOGGER_NAME = "scout.rest.client.logging.loggerName";

  /**
   * Level used for REST client request/response logger.
   * <p>
   * Note: REST client request response logger is activated if at lease one of the LOGGING_LOGGER_* properties is set.
   *
   * @see Level for allowed values
   */
  public static final String LOGGING_LOGGER_LEVEL = "scout.rest.client.logging.loggerLevel";

  /**
   * Verbosity used for REST client request/response logger.
   * <p>
   * Note: REST client request response logger is activated if at lease one of the LOGGING_LOGGER_* properties is set.
   *
   * @see LoggerVerbosity for set of allowed values
   */
  public static final String LOGGING_LOGGER_VERBOSITY = "scout.rest.client.logging.loggerVerbosity";

  /**
   * Maximum number of bytes of an entity to be logged by request/response logger.
   * <p>
   * Note: REST client request response logger is activated if at lease one of the LOGGING_LOGGER_* properties is set.
   */
  public static final String LOGGING_LOGGER_MAX_ENTITY_SIZE = "scout.rest.client.logging.loggerEntityMaxSize";

  /**
   * @see RestClientProperties#LOGGING_LOGGER_VERBOSITY
   */
  public enum LoggerVerbosity {
    /**
     * Only content of HTTP headers is logged. No message payload data are logged.
     */
    HEADERS_ONLY,
    /**
     * Content of HTTP headers as well as entity content of textual media types is logged.
     */
    PAYLOAD_TEXT,
    /**
     * Full verbose logging. Content of HTTP headers as well as any message payload content will be logged.
     */
    PAYLOAD_ANY
  }

  /**
   * Optional custom {@link ICancellable} used by HTTP connection providers that support cancellation.
   */
  public static final String CANCELLABLE = "scout.rest.client.cancellable";

  /**
   * Implementation to use for encoding the request URI, i.e. the URI used in the request line (GET [request-uri]
   * HTTP/1.1)
   * <p>
   * The value MUST implement {@link org.eclipse.scout.rt.rest.IRestHttpRequestUriEncoder}.
   * <p>
   * The default value is {@code null}, meaning the http client's default implementation is used.
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String REQUEST_URI_ENCODER = "scout.rest.client.requestUriEncoder";

  /**
   * The property defines a URI of a HTTP proxy the client connector should use.
   * <p>
   * If the port component of the URI is absent then a default port of {@code 8080} is assumed. If the property absent
   * then the default Scout ConfigurableProxySelector will be utilized.
   * </p>
   * <p>
   * The value MUST be an instance of {@link String}.
   * </p>
   * <p>
   * The default value is {@code null}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String PROXY_URI = "scout.rest.client.proxy.uri";

  /**
   * The property defines a user name which will be used for HTTP proxy authentication.
   * <p>
   * The property is ignored if no {@link #PROXY_URI HTTP proxy URI} has been set. If the property absent then no proxy
   * authentication will be utilized.
   * </p>
   * <p>
   * The value MUST be an instance of {@link String}.
   * </p>
   * <p>
   * The default value is {@code null}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String PROXY_USERNAME = "scout.rest.client.proxy.username";

  /**
   * The property defines a user password which will be used for HTTP proxy authentication.
   * <p>
   * The property is ignored if no {@link #PROXY_URI HTTP proxy URI} has been set. If the property absent then no proxy
   * authentication will be utilized.
   * </p>
   * <p>
   * The value MUST be an instance of {@link String}.
   * </p>
   * <p>
   * The default value is {@code null}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  @SuppressWarnings("squid:S2068")
  public static final String PROXY_PASSWORD = "scout.rest.client.proxy.password";

  /**
   * Automatic redirection. A value of {@code true} declares that the client will automatically redirect to the URI
   * declared in 3xx responses.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * </p>
   * <p>
   * The default value is {@code true}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String FOLLOW_REDIRECTS = "scout.rest.client.followRedirects";

  /**
   * The Scout REST client adds a default user agent header with value {@code 'Generic'} unless a specific user agent
   * header was set at request level by the invoker.<br>
   * If this property is set to {@code true}, the default behavior is suppressed and no user agent header is added
   * automatically by the client.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * </p>
   * <p>
   * The default value is {@code false}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String SUPPRESS_DEFAULT_USER_AGENT = "scout.rest.client.suppressDefaultUserAgent";

  /**
   * Specifies the maximum lifetime in milliseconds for kept alive connections of the REST HTTP client.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Long}.
   * </p>
   * <p>
   * The default value is 30 * 60 * 1000 (e.g. 30 minutes).
   * </p>
   */
  public static final String CONNECTION_KEEP_ALIVE = "scout.rest.client.http.connectionKeepAlive";

  /**
   * Specifies the default maximum connections per route of the REST HTTP client.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Integer}.
   * </p>
   * <p>
   * The default value is 32.
   * </p>
   */
  public static final String MAX_CONNECTIONS_PER_ROUTE = "scout.rest.client.http.maxConnectionsPerRoute";

  /**
   * Specifies the total maximum connections of the REST HTTP client
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Integer}.
   * </p>
   * <p>
   * The default value is 128.
   * </p>
   */
  public static final String MAX_CONNECTIONS_TOTAL = "scout.rest.client.http.maxConnectionsTotal";

  /**
   * Defines period of inactivity in milliseconds after which persistent connections must be re-validated prior to being
   * leased to the consumer. Non-positive value passed to this method disables connection validation. This check helps
   * detect connections that have become stale (half-closed) while kept inactive in the pool.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Integer}.
   * </p>
   * <p>
   * The default value is 1 ms.
   * </p>
   */
  public static final String VALIDATE_CONNECTION_AFTER_INACTIVITY = "scout.rest.client.http.validateAfterInactivity";

  /**
   * Connect timeout interval, in milliseconds. This property is supported either on rest client level (e.g. for all
   * calls) or on a request level (e.g. for a single call).
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Long}. A value of zero (0) is equivalent to an
   * interval of infinity.
   * </p>
   * <p>
   * The default value is infinity (0).
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String CONNECT_TIMEOUT = "scout.rest.client.connectTimeout";

  /**
   * Read timeout interval, in milliseconds. This property is supported either on rest client level (e.g. for all calls)
   * or on a request level (e.g. for a single call).
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Long}. A value of zero (0) is equivalent to an
   * interval of infinity.
   * </p>
   * <p>
   * The default value is infinity (0).
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   * </p>
   */
  public static final String READ_TIMEOUT = "scout.rest.client.readTimeout";
}
