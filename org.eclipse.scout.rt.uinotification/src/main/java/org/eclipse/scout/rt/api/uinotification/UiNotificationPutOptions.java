/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
  private Long  m_timeout;

  /**
   * @param timeout Time in milliseconds before the notification expires and can be removed from the registry by the cleanup job.
   */
  public UiNotificationPutOptions withTimeout(Long timeout) {
    m_timeout = timeout;
    return this;
  }

  public Long getTimeout() {
    return m_timeout;
  }

  public UiNotificationPutOptions withTransactional(Boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  public Boolean getTransactional() {
    return m_transactional;
  }

  public static UiNotificationPutOptions noTransaction() {
    return new UiNotificationPutOptions().withTransactional(false);
  }
}
