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
import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

public class ServiceTunnelResponse implements IServiceTunnelResponse {
  private static final long serialVersionUID = 0L;

  private transient int m_httpCode;
  private transient String m_soapOperation;
  private Object m_data;
  private Object[] m_outVars;
  private Throwable m_exception;
  private Set<IClientNotification> m_clientNotifications;
  private Object m_metaData;
  // added in 3.1.17
  private Long m_processingDuration;

  // for scout serialization
  public ServiceTunnelResponse() {
  }

  public ServiceTunnelResponse(Object data, Object[] outVars, Throwable t) {
    this(0, data, outVars, t);
  }

  public ServiceTunnelResponse(int httpCode, Object data, Object[] outVars, Throwable t) {
    m_httpCode = httpCode;
    m_data = data;
    if (outVars != null) {
      m_outVars = outVars;
    }
    m_exception = t;
  }

  @Override
  public String getSoapOperation() {
    return m_soapOperation;
  }

  public void setSoapOperation(String soapOperation) {
    m_soapOperation = soapOperation;
  }

  @Override
  public int getHttpCode() {
    return m_httpCode;
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
  public Set<IClientNotification> getClientNotifications() {
    return CollectionUtility.hashSet(m_clientNotifications);
  }

  public void setClientNotifications(Collection<? extends IClientNotification> clientNotifications) {
    m_clientNotifications = CollectionUtility.hashSet(clientNotifications);
  }

  /**
   * @return
   * @deprecated extend the {@link ServiceTunnelResponse} and add your own attrubutes will be removed with V5.0
   */
  @Deprecated
  public Object getMetaData() {
    return m_metaData;
  }

  public void setMetaData(Object o) {
    m_metaData = o;
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
    StringBuffer buf = new StringBuffer();
    buf.append("Response[data=" + m_data + ", vars=" + Arrays.asList(m_outVars) + ", exception=" + m_exception + "]");
    return buf.toString();
  }

  /*
   * //Activate for Null-Proxy-Test only private void
   * readObject(ObjectInputStream in) throws IOException,
   * ClassNotFoundException{ //don't call defaultReadObject() }
   */

}
