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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cluster sync status info for the current server node
 * <p>
 * This class is thread safe
 */
public class ClusterNodeStatusInfo {

  private AtomicLong m_sentMessageCount;
  private AtomicLong m_receivedMessageCount;

  private volatile Date m_lastChangedDate;
  private volatile String m_lastChangedUserId;
  private volatile String m_lastChangedOriginNodeId;

  public ClusterNodeStatusInfo() {
    m_sentMessageCount = new AtomicLong();
    m_receivedMessageCount = new AtomicLong();
  }

  public long getSentMessageCount() {
    return m_sentMessageCount.get();
  }

  public long incrementSentMessageCount() {
    return m_sentMessageCount.incrementAndGet();
  }

  public long addSentMessageCount(long delta) {
    return m_sentMessageCount.addAndGet(delta);
  }

  public long getReceivedMessageCount() {
    return m_receivedMessageCount.get();
  }

  public long incrementReceivedMessageCount() {
    return m_receivedMessageCount.incrementAndGet();
  }

  public long addReceivedMessageCount(long delta) {
    return m_receivedMessageCount.addAndGet(delta);
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

  public String getLastChangedOriginNodeId() {
    return m_lastChangedOriginNodeId;
  }

  public void setLastChangedOriginNodeId(String lastChangedOriginNodeId) {
    m_lastChangedOriginNodeId = lastChangedOriginNodeId;
  }

  public void updateReceiveStatus(IClusterNotificationMessage message) {
    incrementReceivedMessageCount();
    setLastChangedDate(new Date());
    setLastChangedUserId(message.getProperties().getOriginUser());
    setLastChangedOriginNodeId(message.getProperties().getOriginNode());
  }
}
