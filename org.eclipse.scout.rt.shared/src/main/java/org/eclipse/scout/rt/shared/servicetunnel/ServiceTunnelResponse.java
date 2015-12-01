/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

public class ServiceTunnelResponse implements Serializable {
  private static final long serialVersionUID = 0L;

  private final Object m_data;
  private final Object[] m_outVars;
  private final Throwable m_exception;
  // added in 3.1.17
  private volatile Long m_processingDuration;

  private List<ClientNotificationMessage> m_notifications;

  public ServiceTunnelResponse(Throwable t) {
    this(null, null, t);
  }

  public ServiceTunnelResponse(Object data, Object[] outVars) {
    this(data, outVars, null);
  }

  public ServiceTunnelResponse(Object data, Object[] outVars, Throwable t) {
    m_data = data;
    m_outVars = outVars;
    m_exception = t;
  }

  public Object getData() {
    return m_data;
  }

  public Object[] getOutVars() {
    return m_outVars;
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
    tsb.attr("vars", Arrays.asList(getOutVars()));
    tsb.attr("exception", getException());
    tsb.attr("notifications", getNotifications());
    return tsb.toString();
  }

}
