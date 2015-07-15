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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

public class ServiceTunnelResponse implements IServiceTunnelResponse {
  private static final long serialVersionUID = 0L;

  private final Object m_data;
  private final Object[] m_outVars;
  private final Throwable m_exception;
  // added in 3.1.17
  private volatile Long m_processingDuration;

  private List<ClientNotificationMessage> m_notifications;
  private String m_userId;

  public ServiceTunnelResponse(Object data, Object[] outVars, Throwable t) {
    m_data = data;
    m_outVars = outVars;
    m_exception = t;
  }

  @Override
  public Object getData() {
    return m_data;
  }

  @Override
  public Object[] getOutVars() {
    return m_outVars;
  }

  @Override
  public Throwable getException() {
    return m_exception;
  }

  @Override
  public Long getProcessingDuration() {
    return m_processingDuration;
  }

  public void setProcessingDuration(Long millis) {
    m_processingDuration = millis;
  }

  /**
   * Piggyback notifications. Transactional notifications can be piggybacked to the client with the corresponding
   * {@link ServiceTunnelResponse}.
   *
   * @param notifications
   */
  @Override
  public synchronized void setNotifications(List<ClientNotificationMessage> notifications) {
    m_notifications = notifications;
  }

  @Override
  public synchronized List<ClientNotificationMessage> getNotifications() {
    return m_notifications;
  }

  @Override
  public synchronized void setUserId(String userId) {
    m_userId = userId;
  }

  @Override
  public synchronized String getUserId() {
    return m_userId;
  }

  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this);
    tsb.ref("data", getData());
    tsb.attr("vars", Arrays.asList(getOutVars()));
    tsb.attr("exception", getException());
    tsb.attr("userId", getUserId());
    tsb.attr("notifications", getNotifications());
    return tsb.toString();
  }

}
