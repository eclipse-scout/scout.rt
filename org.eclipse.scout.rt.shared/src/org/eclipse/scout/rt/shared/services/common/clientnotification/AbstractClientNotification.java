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
  //This is only for demonstration reasons
  private String m_receiveingServerNodeId;
  private String m_providingServerNodeId;

  private String m_id;

  public AbstractClientNotification() {
    m_id = UUID.randomUUID().toString();
  }

  @Override
  public int getOriginNode() {
    return m_node;
  }

  @Override
  public void setOriginNode(int node) {
    m_node = node;
  }

  @Override
  public String getReceiveingServerNodeId() {
    return m_receiveingServerNodeId;
  }

  @Override
  public void setReceiveingServerNodeId(String receiveingServerNodeId) {
    m_receiveingServerNodeId = receiveingServerNodeId;
  }

  @Override
  public String getProvidingServerNodeId() {
    return m_providingServerNodeId;
  }

  @Override
  public void setProvidingServerNodeId(String providingServerNodeId) {
    m_providingServerNodeId = providingServerNodeId;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public String getId() {
    return m_id;
  }
}
