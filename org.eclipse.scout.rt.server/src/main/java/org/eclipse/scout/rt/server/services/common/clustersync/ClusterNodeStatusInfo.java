/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.util.Date;

/**
 * Cluster sync status info for the current server node
 * <p>
 * This class is thread safe
 */
public class ClusterNodeStatusInfo {

  private final ClusterNodeStatusBean m_info = new ClusterNodeStatusBean();

  //lock for m_info
  private final Object m_lock = new Object();

  /**
   * @return the current status.
   */
  public IClusterNodeStatusInfo getStatus() {
    synchronized (m_lock) {
      return m_info.copy();
    }
  }

  /**
   * Update the status when a message is sent.
   *
   * @param sentMessage
   */
  public void updateSentStatus(IClusterNotificationMessage sentMessage) {
    synchronized (m_lock) {
      m_info.incSentMessageCount();
      updateLastChanged(sentMessage);
    }
  }

  /**
   * Update the status when a message is received.
   *
   * @param receivedMessage
   */
  public void updateReceiveStatus(IClusterNotificationMessage receivedMessage) {
    synchronized (m_lock) {
      m_info.incReceivedMessageCount();
      updateLastChanged(receivedMessage);
    }
  }

  /**
   * Updates last changed node information
   */
  public void updateLastChanged(String userId, String originNode) {
    synchronized (m_lock) {
      m_info.setLastChangedDate(new Date());
      m_info.setLastChangedUserId(userId);
      m_info.setLastChangedOriginNodeId(originNode);
    }
  }

  private void updateLastChanged(IClusterNotificationMessage message) {
    updateLastChanged(message.getProperties().getOriginUser(), message.getProperties().getOriginNode());
  }

  private static class ClusterNodeStatusBean implements IClusterNodeStatusInfo {

    private long m_sentMessageCount;

    private long m_receivedMessageCount;

    private Date m_lastChangedDate;
    private String m_lastChangedUserId;
    private String m_lastChangedOriginNodeId;

    public ClusterNodeStatusBean() {
      super();
    }

    protected ClusterNodeStatusBean(ClusterNodeStatusBean other) {
      m_sentMessageCount = other.m_sentMessageCount;
      m_receivedMessageCount = other.m_receivedMessageCount;
      m_lastChangedDate = other.m_lastChangedDate;
      m_lastChangedUserId = other.m_lastChangedUserId;
      m_lastChangedOriginNodeId = other.m_lastChangedOriginNodeId;
    }

    @Override
    public long getSentMessageCount() {
      return m_sentMessageCount;
    }

    public void incSentMessageCount() {
      m_sentMessageCount++;
    }

    @Override
    public long getReceivedMessageCount() {
      return m_receivedMessageCount;
    }

    public void incReceivedMessageCount() {
      m_receivedMessageCount++;
    }

    @Override
    public Date getLastChangedDate() {
      return m_lastChangedDate;
    }

    public void setLastChangedDate(Date lastChangedDate) {
      m_lastChangedDate = lastChangedDate;
    }

    @Override
    public String getLastChangedUserId() {
      return m_lastChangedUserId;
    }

    public void setLastChangedUserId(String lastChangedUserId) {
      m_lastChangedUserId = lastChangedUserId;
    }

    @Override
    public String getLastChangedOriginNodeId() {
      return m_lastChangedOriginNodeId;
    }

    public void setLastChangedOriginNodeId(String lastChangedOriginNodeId) {
      m_lastChangedOriginNodeId = lastChangedOriginNodeId;
    }

    /**
     * Creates a shallow copy of this instance.
     */
    public ClusterNodeStatusBean copy() {
      return new ClusterNodeStatusBean(this);
    }

    @Override
    public String toString() {
      return "ClusterNodeStatusBean [sentMessageCount=" + m_sentMessageCount + ", receivedMessageCount=" + m_receivedMessageCount + ", lastChangedDate=" + m_lastChangedDate + ", lastChangedUserId=" + m_lastChangedUserId
          + ", lastChangedOriginNodeId=" + m_lastChangedOriginNodeId + "]";
    }
  }
}
