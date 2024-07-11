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

public class UiNotificationPutOptions {
  private Boolean m_transactional;
  private Long m_timeout;
  private Boolean m_publishOverCluster;

  public UiNotificationPutOptions copy() {
    return new UiNotificationPutOptions()
        .withTransactional(getTransactional())
        .withTimeout(getTimeout())
        .withPublishOverCluster(getPublishOverCluster());
  }

  /**
   * @param timeout
   *          Time in milliseconds before the notification expires and can be removed from the registry by the cleanup
   *          job.
   */
  public UiNotificationPutOptions withTimeout(Long timeout) {
    m_timeout = timeout;
    return this;
  }

  public Long getTimeout() {
    return m_timeout;
  }

  /**
   * @param transactional
   *          Whether the notification should be put into the registry only if the current transaction is committed
   *          successfully or not. Default is {@code true}.
   */
  public UiNotificationPutOptions withTransactional(Boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  public Boolean getTransactional() {
    return m_transactional;
  }

  /**
   * @param publishOverCluster
   *          Whether the notification is published to all other cluster nodes or not. Default is {@code true}.
   */
  public UiNotificationPutOptions withPublishOverCluster(Boolean publishOverCluster) {
    m_publishOverCluster = publishOverCluster;
    return this;
  }

  public Boolean getPublishOverCluster() {
    return m_publishOverCluster;
  }

  /*
   * factory methods
   */

  /**
   * Creates an {@link UiNotificationPutOptions} instance with {@code transactional = false}.
   */
  public static UiNotificationPutOptions noTransaction() {
    return new UiNotificationPutOptions().withTransactional(false);
  }

  /**
   * Creates an {@link UiNotificationPutOptions} instance with {@code publishOverCluster = false}.
   */
  public static UiNotificationPutOptions noClusterSync() {
    return new UiNotificationPutOptions().withPublishOverCluster(false);
  }
}
