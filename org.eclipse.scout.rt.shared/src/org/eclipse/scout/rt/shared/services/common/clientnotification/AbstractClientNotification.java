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
package org.eclipse.scout.rt.shared.services.common.clientnotification;

import java.util.UUID;

public abstract class AbstractClientNotification implements IClientNotification {
  private static final long serialVersionUID = 1L;
  private int m_node;
  private final String m_notificationId = UUID.randomUUID().toString();
  private final long m_timeout;
  private String m_originServerNode;
  private String m_providingServerNodeId;
  private static final long DEFAULT_TIMEOUT = 1000 * 60 * 10; // 10 min

  public AbstractClientNotification(long timeout) {
    m_timeout = timeout;
  }

  public AbstractClientNotification() {
    this(DEFAULT_TIMEOUT);
  }

  @Override
  public long getTimeout() {
    return m_timeout;
  }

  @Override
  public String getId() {
    return m_notificationId;
  }

  /**
   * @deprecated use {@link #getOriginalServerNode()}. Will be removed in Release 5.0
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public int getOriginNode() {
    return m_node;
  }

  /**
   * @deprecated use {@link #getOriginalServerNode()}. Will be removed in Release 5.0
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setOriginNode(int node) {
    m_node = node;
    m_originServerNode = String.valueOf(node);
  }

  @Override
  public String getOriginalServerNode() {
    return m_originServerNode;
  }

  @Override
  public void setOriginalServerNode(String originServerNodeId) {
    m_originServerNode = originServerNodeId;
  }

  @Override
  public String getProvidingServerNode() {
    return m_providingServerNodeId;
  }

  @Override
  public void setProvidingServerNode(String providingServerNodeId) {
    m_providingServerNodeId = providingServerNodeId;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
