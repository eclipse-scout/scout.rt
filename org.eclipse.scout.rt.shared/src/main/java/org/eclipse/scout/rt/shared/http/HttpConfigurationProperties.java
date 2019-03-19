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
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;

public final class HttpConfigurationProperties {

  private HttpConfigurationProperties() {
  }

  public static class ApacheHttpTransportConnectionTimeToLiveProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 1000 * 60 * 60; // default: 1 hour
    }

    @Override
    public String description() {
      return "Specifies the maximum life time in milliseconds for kept alive connections of the Apache HTTP client. The default value is 1 hour.";
    }

    @Override
    public String getKey() {
      return "scout.http.connectionTtl";
    }
  }

  public static class ApacheHttpTransportMaxConnectionsPerRouteProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 32;
    }

    @Override
    public String description() {
      return "Configuration property to define the default maximum connections per route of the Apache HTTP client. The default value is 32.";
    }

    @Override
    public String getKey() {
      return "scout.http.maxConnectionsPerRoute";
    }
  }

  public static class ApacheHttpTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 128;
    }

    @Override
    public String description() {
      return "Specifies the total maximum connections of the Apache HTTP client. The default value is 128.";
    }

    @Override
    public String getKey() {
      return "scout.http.maxConnectionsTotal";
    }
  }

  public static class ApacheHttpTransportKeepAliveProperty extends AbstractBooleanConfigProperty {

    public static final String HTTP_KEEP_ALIVE = "http.keepAlive";

    @Override
    public Boolean getDefaultValue() {
      String prop = System.getProperty(HTTP_KEEP_ALIVE);
      return prop != null ? Boolean.valueOf(prop) : Boolean.TRUE;
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return String.format("Enable/disable HTTP keep-alive connections.\n"
          + "The default value is defined by the system property '%s' or true if the system property is undefined.", HTTP_KEEP_ALIVE);
    }

    @Override
    public String getKey() {
      return "scout.http.keepAlive";
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
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return "Enable retry of request (includes non-idempotent requests) on NoHttpResponseException\n"
          + "Assuming that the cause of the exception was most probably a stale socket channel on the server side.\n"
          + "For apache tomcat see http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659\n"
          + "The default value is true";
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
    public Boolean getDefaultValue() {
      return true;
    }

    @Override
    @SuppressWarnings("findbugs:VA_FORMAT_STRING_USES_NEWLINE")
    public String description() {
      return "Enable retry of request (includes non-idempotent requests) on {@link SocketException} with message 'Connection reset'\n"
          + "Assuming that the cause of the exception was most probably a stale socket channel on the server side.\n"
          + "For apache tomcat see http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659\n"
          + "The default value is true";
    }

    @Override
    public String getKey() {
      return "scout.http.retryOnSocketExceptionByConnectionReset";
    }
  }
}
