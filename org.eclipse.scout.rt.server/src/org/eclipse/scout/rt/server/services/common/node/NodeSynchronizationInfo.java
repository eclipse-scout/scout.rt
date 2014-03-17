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
package org.eclipse.scout.rt.server.services.common.node;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.server.services.common.notification.INotificationService;

/**
 *
 */
public class NodeSynchronizationInfo {

  private INotificationService m_clusterSyncService;
  private AtomicLong m_messageNrSequence;
  private AtomicLong m_sentMessageCount;
  private AtomicLong m_receivedMessageCount;

  private Date m_lastChangedDate;
  private String m_lastChangedUserId;
  private String m_lastChangedClusterNodeId;

  public NodeSynchronizationInfo() {
    m_messageNrSequence = new AtomicLong();
    m_sentMessageCount = new AtomicLong();
    m_receivedMessageCount = new AtomicLong();
  }

  /**
   * @return true if an enabled {@link IClusterSynchronizationJmsAdapterService} was set in
   *         {@link #setClusterSyncService(IClusterSynchronizationJmsAdapterService)}
   */
  public boolean isEnabled() {
    return m_clusterSyncService != null && m_clusterSyncService.isEnabled();
  }

  /**
   * @return the active (possibly null) {@link IClusterSynchronizationJmsAdapterService} that is enabled, so caller
   *         needs not check for {@link IClusterSynchronizationJmsAdapterService#isEnabled()}
   *         <p>
   *         {@link IClusterSynchronizationJmsAdapterService#register()} sets the service onto this info helper
   */
  public INotificationService getClusterSyncService() {
    return m_clusterSyncService;
  }

  public void setClusterSyncService(INotificationService clusterSyncService) {
    m_clusterSyncService = clusterSyncService;
  }

  public long getNewMessageNr() {
    return m_messageNrSequence.getAndIncrement();
  }

  public long getSentMessageCount() {
    return m_sentMessageCount.get();
  }

  public long getReceivedMessageCount() {
    return m_receivedMessageCount.get();
  }

  public long incrementSentMessageCount() {
    return m_sentMessageCount.incrementAndGet();
  }

  public long incrementReceivedMessageCount() {
    return m_receivedMessageCount.incrementAndGet();
  }

  public Date getLastChangedDate() {
    return m_lastChangedDate;
  }

  public void setLastChangedDate(Date lastChangedDate) {
    m_lastChangedDate = lastChangedDate;
  }

  public String getLastChangedUserId() {
    return m_lastChangedUserId;
  }

  public void setLastChangedUserId(String lastChangedUserId) {
    m_lastChangedUserId = lastChangedUserId;
  }

  public String getLastChangedClusterNodeId() {
    return m_lastChangedClusterNodeId;
  }

  public void setLastChangedClusterNodeId(String lastChangedClusterNodeId) {
    m_lastChangedClusterNodeId = lastChangedClusterNodeId;
  }
}
