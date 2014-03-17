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
package org.eclipse.scout.rt.server.services.common.notification;

import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.shared.services.common.node.INodeSynchronizationProcessService;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class DistributedNotification implements IDistributedNotification {

  private static final long serialVersionUID = -868652571746741488L;
  private INotification m_notification;
  private DistributedNotificationType m_distributedNotificationType;
  private String m_originNode;
  private String m_originUser;

  public DistributedNotification(INotification notification, DistributedNotificationType type) {
    m_notification = notification;
    m_distributedNotificationType = type;
    m_originNode = SERVICES.getService(INodeSynchronizationProcessService.class).getClusterNodeId();
    m_originUser = (ThreadContext.getServerSession() != null) ? ThreadContext.getServerSession().getUserId() : null;
  }

  public enum DistributedNotificationType {
    NEW, UPDATE, REMOVE
  }

  @Override
  public INotification getNotification() {
    return m_notification;
  }

  @Override
  public boolean isNew() {
    return m_distributedNotificationType
        .equals(DistributedNotificationType.NEW);
  }

  @Override
  public boolean isUpdate() {
    return m_distributedNotificationType
        .equals(DistributedNotificationType.UPDATE);
  }

  @Override
  public boolean isRemove() {
    return m_distributedNotificationType
        .equals(DistributedNotificationType.REMOVE);
  }

  @Override
  public String getOriginNode() {
    return m_originNode;
  }

  @Override
  public String getOriginUser() {
    return m_originUser;
  }

}
