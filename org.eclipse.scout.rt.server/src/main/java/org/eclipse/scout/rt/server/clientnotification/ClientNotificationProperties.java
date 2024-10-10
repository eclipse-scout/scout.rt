/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;

/**
 * Configuration properties for client notifications
 */
public final class ClientNotificationProperties {

  private ClientNotificationProperties() {
  }

  public static class NodeQueueCapacity extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 10000;
    }

    @Override
    public String description() {
      return "Capacity of the client notification queue. If maximum capacity is reached, notification messages are dropped. The default value is 10000.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.nodeQueueCapacity";
    }
  }

  public static class MaxNotificationBlockingTimeOut extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 10000;
    }

    @Override
    public String description() {
      return "The maximum amount of time in millisecons a consumer blocks while waiting for new notifications. The default is 10 seconds.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.maxNotificationBlockingTimeOut";
    }
  }

  public static class MaxNotificationMessages extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 100;
    }

    @Override
    public String description() {
      return "The maximum number of client notifications that are consumed at once. The default is 100.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.chunkSize";
    }
  }

  public static class NotificationQueueExpireTime extends AbstractPositiveIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 10 * 60 * 1000;
    }

    @Override
    public String description() {
      return "If no message is consumed for the specified number of milliseconds, client notification queues (with possibly pending notifications) are removed.\n"
          + "This avoids overflows and unnecessary memory consumption. Old queues may exist if a node does not properly unregister (e.g. due to a crash).\n"
          + "The default value is 10 minutes.";
    }

    @Override
    public String getKey() {
      return "scout.clientnotification.notificationQueueExpireTime";
    }
  }
}
