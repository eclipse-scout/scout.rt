/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

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
      return "Specifies the maximum life time in milliseconds for kept alive connections of the Apache HTTP client. The defautl value is 1 hour.";
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
      return prop != null ? Boolean.valueOf(prop) : true;
    }

    @Override
    public String description() {
      return String.format("Enable/disable HTTP keep-alive connections.\n"
          + "The default value is defined by the system property '%s' or true if the system property is undefined.", HTTP_KEEP_ALIVE);
    }

    @Override
    public String getKey() {
      return "scout.http.keepAlive";
    }
  }

  public static class ApacheHttpTransportRetryPostProperty extends AbstractBooleanConfigProperty {

    public static final String SUN_NET_HTTP_RETRY_POST = "sun.net.http.retryPost";

    @Override
    public Boolean getDefaultValue() {
      String prop = System.getProperty(SUN_NET_HTTP_RETRY_POST);
      return prop != null ? Boolean.valueOf(prop) : true;
    }

    @Override
    public String description() {
      return String.format("Enable or disable one retry for non-idempotent POST requests.\n"
          + "The default value is defined by the system property '%s' or true if the system property is undefined.", SUN_NET_HTTP_RETRY_POST);
    }

    @Override
    public String getKey() {
      return "scout.http.retryHost";
    }
  }
}
