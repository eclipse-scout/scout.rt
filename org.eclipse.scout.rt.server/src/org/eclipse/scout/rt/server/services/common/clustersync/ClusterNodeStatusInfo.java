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
 */
public class ClusterNodeStatusInfo implements IClusterNodeStatusInfo {

  private AtomicLong m_sentMessageCount;
  private AtomicLong m_receivedMessageCount;

  private volatile Date m_lastChangedDate;
  private volatile String m_lastChangedUserId;
  private volatile String m_lastReceivedOriginNode;

  public ClusterNodeStatusInfo() {
    m_sentMessageCount = new AtomicLong();
    m_receivedMessageCount = new AtomicLong();
  }

  @Override
  public long getSentMessageCount() {
    return m_sentMessageCount.get();
  }

  @Override
  public long getReceivedMessageCount() {
    return m_receivedMessageCount.get();
  }

  public long incrementSentMessageCount() {
    return m_sentMessageCount.incrementAndGet();
  }

  public long incrementReceivedMessageCount() {
    return m_receivedMessageCount.incrementAndGet();
  }

  @Override
  public Date getLastReceivedDate() {
    return m_lastChangedDate;
  }

  @Override
  public String getLastReceivedOriginUser() {
    return m_lastChangedUserId;
  }

  @Override
  public String getLastReceivedOriginNode() {
    return m_lastReceivedOriginNode;
  }

  public void updateReceiveStatus(IClusterNotificationMessage message) {
    incrementReceivedMessageCount();
    setLastChangedDate(new Date());
    setLastReceivedOriginUser(message.getProperties().getOriginUser());
    setLastReceivedOriginNode(message.getProperties().getOriginNode());
  }

  public void setLastChangedDate(Date lastChangedDate) {
    m_lastChangedDate = lastChangedDate;
  }

  public void setLastReceivedOriginUser(String lastChangedUserId) {
    m_lastChangedUserId = lastChangedUserId;
  }

  public void setLastReceivedOriginNode(String lastReceivedOriginNode) {
    m_lastReceivedOriginNode = lastReceivedOriginNode;
  }
}
