/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.util.List;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

public class ServiceTunnelResponse implements Serializable {
  private static final long serialVersionUID = 0L;

  private final Object m_data;
  private final Throwable m_exception;
  // added in 3.1.17
  private volatile Long m_processingDuration;

  private List<ClientNotificationMessage> m_notifications;

  public ServiceTunnelResponse(Throwable t) {
    this(null, t);
  }

  public ServiceTunnelResponse(Object data) {
    this(data, null);
  }

  public ServiceTunnelResponse(Object data, Throwable t) {
    m_data = data;
    m_exception = t;
  }

  public Object getData() {
    return m_data;
  }

  public Throwable getException() {
    return m_exception;
  }

  public synchronized Long getProcessingDuration() {
    return m_processingDuration;
  }

  public synchronized void setProcessingDuration(Long millis) {
    m_processingDuration = millis;
  }

  /**
   * Piggyback notifications. Transactional notifications can be piggybacked to the client with the corresponding
   * {@link ServiceTunnelResponse}.
   *
   * @param notifications
   */
  public synchronized void setNotifications(List<ClientNotificationMessage> notifications) {
    m_notifications = notifications;
  }

  public synchronized List<ClientNotificationMessage> getNotifications() {
    return m_notifications;
  }

  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this);
    tsb.ref("data", getData());
    tsb.attr("exception", getException());
    tsb.attr("notifications", getNotifications());
    return tsb.toString();
  }
}
