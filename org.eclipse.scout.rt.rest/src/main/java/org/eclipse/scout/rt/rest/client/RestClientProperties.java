/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client;

public final class RestClientProperties {

  /**
   * A value of {@code true} enables cookies.
   * <p>
   * The value MUST be an instance convertible to {@link java.lang.Boolean}.
   * </p>
   * <p>
   * The default value is {@code true}.
   * </p>
   * <p>
   * The name of the configuration property is <tt>{@value}</tt>.
   */
  public static final String ENABLE_COOKIES = "scout.rest.client.enableCookies";

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
   * @see {@code java.util.logging.Level} for allowed values
   */
  public static final String LOGGING_LOGGER_LEVEL = "scout.rest.client.logging.loggerLevel";

  /**
   * Verbosity used for REST client request/response logger.
   * <p>
   * Note: REST client request response logger is activated if at lease one of the LOGGING_LOGGER_* properties is set.
   *
   * @see {@code LoggerVerbosity} for set of allowed values
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
}
