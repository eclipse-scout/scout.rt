/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;

public final class UiNotificationConfigProperties {

  @ApiExposed
  public static class UiNotificationWaitTimeoutProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.uinotification.waitTimeout";
    }

    @Override
    public String description() {
      return "Configures in milliseconds how long the request should listen for a new notification it is interested in is put into the registry."
          + "If set to 0, the currently available list of notifications will be returned immediately."
          + "Default is 1 minute.";
    }

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toMillis(1);
    }
  }

  public static class RegistryCleanupJobIntervalProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.uinotification.cleanupJobInterval";
    }

    @Override
    public String description() {
      return "The cleanup job removes expired notifications from the registry."
          + "This property configures in seconds, how often the cleanup job should run. Default is set to 5 minutes."
          + "If set to 0, the job won't run.";
    }

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toSeconds(5);
    }
  }

  public static class UiNotificationExpirationTimeProperty extends AbstractLongConfigProperty {

    @Override
    public String getKey() {
      return "scout.uinotification.expirationTime";
    }

    @Override
    public String description() {
      return "Configures in milliseconds how long the notification has to be kept in the registry until it can be removed."
          + "This value is used if no explicit expirationTime is set for a specific notification."
          + "Important: the expiration time should be larger than the timeout for the HTTP session used for the UI."
          + "This makes sure he won't miss any notifications if he was offline for a while, because he will be logged out anyway after getting online again."
          + "Default is 7 minutes.";
    }

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toMillis(7);
    }
  }

  public static class NotificationHandlerThroughputProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 30;
    }

    @Override
    public String getKey() {
      return "scout.uinotification.handler.throughput";
    }

    @Override
    public String description() {
      return "Specifies how many requests of browser side UI notification handlers can be processed per second.\n"
          + "The value is used to delay requests from the UI notification handlers to the server.\n"
          + "Use a high value for fast servers. Then the refresh on the browser will be very soon, but peak load on the server is increased.\n"
          + "Use smaller values to reduce server load but increase the delay between the reload on the server and the refresh on the browser.\n"
          + "The default is 30 requests per second.";
    }
  }
}
