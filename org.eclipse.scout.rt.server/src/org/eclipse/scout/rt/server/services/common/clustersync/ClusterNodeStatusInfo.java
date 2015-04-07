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
      return m_info.clone();
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

  private static class ClusterNodeStatusBean implements IClusterNodeStatusInfo, Cloneable {

    private long m_sentMessageCount;

    private long m_receivedMessageCount;

    private Date m_lastChangedDate;
    private String m_lastChangedUserId;
    private String m_lastChangedOriginNodeId;

    @Override
    public long getSentMessageCount() {
      return m_sentMessageCount;
    }

    /**
     * @param delta
     */
    public void incReveivedMessageCount(long delta) {
      m_receivedMessageCount += delta;
    }

    public void incSentMessageCount() {
      m_sentMessageCount++;
    }

    @Deprecated
    void incSentMessageCount(long delta) {
      m_sentMessageCount += delta;
    }

    @Override
    public long getReceivedMessageCount() {
      return m_receivedMessageCount;
    }

    @Deprecated
    void incReceivedMessageCount(long delta) {
      m_receivedMessageCount += delta;
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

    @Override
    protected ClusterNodeStatusBean clone() {
      try {
        return (ClusterNodeStatusBean) super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new UnsupportedOperationException(e);
      }
    }

    @Override
    public String toString() {
      return "ClusterNodeStatusBean [sentMessageCount=" + m_sentMessageCount + ", receivedMessageCount=" + m_receivedMessageCount + ", lastChangedDate=" + m_lastChangedDate + ", lastChangedUserId=" + m_lastChangedUserId + ", lastChangedOriginNodeId=" + m_lastChangedOriginNodeId + "]";
    }

  }

  /**
   * @return number of sent messages
   * @deprecated use {@link #getStatus().getSentMessageCount} Will be removed in the N Release.
   */
  @Deprecated
  public long getSentMessageCount() {
    synchronized (m_lock) {
      return m_info.getSentMessageCount();
    }
  }

  /**
   * @deprecated use {@link #getStatus().getReceivedMessageCount} Will be removed in the N Release.
   */
  @Deprecated
  public long getReceivedMessageCount() {
    synchronized (m_lock) {
      return m_info.getReceivedMessageCount();
    }
  }

  /**
   * @deprecated use {@link #getStatus().getLastChangedDate} Will be removed in the N Release.
   */
  @Deprecated
  public Date getLastChangedDate() {
    synchronized (m_lock) {
      return m_info.getLastChangedDate();
    }
  }

  /**
   * @deprecated use {@link #getStatus().getLastChangedUserId} Will be removed in the N Release.
   */
  @Deprecated
  public String getLastChangedUserId() {
    synchronized (m_lock) {
      return m_info.getLastChangedUserId();
    }
  }

  /**
   * @deprecated use {@link #getStatus().getLastChangedOriginNodeId} Will be removed in the N Release.
   */
  @Deprecated
  public String getLastChangedOriginNodeId() {
    synchronized (m_lock) {
      return m_info.getLastChangedOriginNodeId();
    }
  }

  /**
   * @deprecated use {@link #updateLastChanged(String, String)}. Will be removed in the N Release.
   */
  @Deprecated
  public void setLastChangedDate(Date lastChangedDate) {
    synchronized (m_lock) {
      m_info.setLastChangedDate(lastChangedDate);
    }
  }

  /**
   * @deprecated use {@link #updateLastChanged(String, String)}. Will be removed in the N Release.
   */
  @Deprecated
  public void setLastChangedUserId(String lastChangedUserId) {
    synchronized (m_lock) {
      m_info.setLastChangedUserId(lastChangedUserId);
    }
  }

  /**
   * @deprecated use {@link #updateReceiveStatus(IClusterNotificationMessage)} Will be removed in the N Release.
   */
  @Deprecated
  public long incrementReceivedMessageCount() {
    synchronized (m_lock) {
      m_info.incReceivedMessageCount();
      return m_info.getReceivedMessageCount();
    }
  }

  /**
   * @deprecated use {@link #updateReceiveStatus(IClusterNotificationMessage)} Will be removed in the N Release.
   */
  @Deprecated
  public long incrementReceivedMessageCount(long delta) {
    synchronized (m_lock) {
      m_info.incReceivedMessageCount(delta);
      return m_info.getReceivedMessageCount();
    }
  }

  /**
   * @deprecated use {@link #updateSentStatus(IClusterNotificationMessage)} Will be removed in the N Release.
   */
  @Deprecated
  public long incrementSentMessageCount() {
    synchronized (m_lock) {
      m_info.incSentMessageCount();
      return m_info.getSentMessageCount();
    }
  }

  /**
   * @deprecated use {@link #updateSentStatus(IClusterNotificationMessage)} Will be removed in the N Release.
   */
  @Deprecated
  public long addSentMessageCount(long delta) {
    synchronized (m_lock) {
      m_info.incSentMessageCount(delta);
      return m_info.getSentMessageCount();
    }
  }

  /**
   * @deprecated use {@link #updateReceiveStatus(IClusterNotificationMessage)} Will be removed in the N Release.
   */
  @Deprecated
  public long addReceivedMessageCount(long delta) {
    synchronized (m_lock) {
      m_info.incReveivedMessageCount(delta);
      return m_info.getReceivedMessageCount();
    }
  }

  /**
   * @deprecated use {@link #updateLastChanged(String, String)}. Will be removed in the N Release.
   */
  @Deprecated
  public void setLastChangedOriginNodeId(String originNode) {
    synchronized (m_lock) {
      m_info.setLastChangedOriginNodeId(originNode);
    }
  }

}
