/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;

/**
 * Configuration properties for client notifications
 */
public class ClientNotificationProperties {

  private ClientNotificationProperties() {
  }

  /**
   * Capacity of notification queue for client notifications. If maximum capacity is reached, notification messages are
   * dropped.
   */
  public static class NodeQueueCapacity extends AbstractPositiveIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return Integer.valueOf(200);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.clientnotification.ClientNotificationNodeQueue#capacity";
    }
  }

  /**
   * The maximum amount of time in [ms] a consumer blocks while waiting for new notifications.
   */
  public static class MaxNotificationBlockingTimeOut extends AbstractPositiveIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return Integer.valueOf(10000);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.clientnotification.ClientNotificationService#blockingTimeout";
    }
  }

  /**
   * The maximum number of notifications that are consumed at once.
   */
  public static class MaxNotificationMessages extends AbstractPositiveIntegerConfigProperty {

    @Override
    protected Integer getDefaultValue() {
      return Integer.valueOf(30);
    }

    @Override
    public String getKey() {
      return "org.eclipse.scout.rt.server.clientnotification.ClientNotificationService#maxMessages";
    }
  }

}
