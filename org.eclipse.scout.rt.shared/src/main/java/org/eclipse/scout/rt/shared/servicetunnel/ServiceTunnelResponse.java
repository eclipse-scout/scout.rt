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
import java.util.Set;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

public class ServiceTunnelResponse implements IServiceTunnelResponse {
  private static final long serialVersionUID = 0L;

  private final Object m_data;
  private final Object[] m_outVars;
  private final Throwable m_exception;
  // added in 3.1.17
  private volatile Long m_processingDuration;

  private Set<ClientNotificationMessage> m_notifications;

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

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("Response[data=" + m_data + ", vars=" + ((m_outVars == null) ? "" : Arrays.asList(m_outVars)) + ", exception=" + m_exception + "]");
    return buf.toString();
  }

  /**
   * Piggyback notifications. Transactional notifications are piggybacked to the client with the corresponding
   * {@link ServiceTunnelResponse}.
   *
   * @param notifications
   */
  @Override
  public void setNotifications(Set<ClientNotificationMessage> notifications) {
    m_notifications = notifications;
  }

  @Override
  public Set<ClientNotificationMessage> getNotifications() {
    return m_notifications;
  }

  /*
   * //Activate for Null-Proxy-Test only private void
   * readObject(ObjectInputStream in) throws IOException,
   * ClassNotFoundException{ //don't call defaultReadObject() }
   */

}
