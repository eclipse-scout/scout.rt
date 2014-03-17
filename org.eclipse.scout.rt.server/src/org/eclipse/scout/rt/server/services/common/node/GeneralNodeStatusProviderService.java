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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.service.AbstractService;

/**
 *
 */
public class GeneralNodeStatusProviderService extends AbstractService implements INodeSynchronizationStatusProviderService {

  public static final String GENERAL_CLUSTER_STATUS_PROVIDER_ID = "General";

  @Override
  public NodeServiceStatus getClusterStatus() throws ProcessingException {
    NodeServiceStatus status = new NodeServiceStatus();
    status.setServiceName(GENERAL_CLUSTER_STATUS_PROVIDER_ID);
    status.setDescription(TEXTS.get("General"));
    status.setReloadClusterCacheMenuText("");

    status.setStatusDate(new Date());
    status.setLastChangedDate(Activator.getDefault().getNodeSynchronizationInfo().getLastChangedDate());
    status.setLastChangedUserId(Activator.getDefault().getNodeSynchronizationInfo().getLastChangedUserId());
    status.setLastChangedClusterNodeId(Activator.getDefault().getNodeSynchronizationInfo().getLastChangedClusterNodeId());

    status.addInfoLine("sentMessagesCount", NumberUtility.format(Activator.getDefault().getNodeSynchronizationInfo().getSentMessageCount()) + "");
    status.addInfoLine("receivedMessagesCount", NumberUtility.format(Activator.getDefault().getNodeSynchronizationInfo().getReceivedMessageCount()) + "");

    return status;
  }

  @Override
  public boolean reloadClusterCache(String resourceName) throws ProcessingException {
    if (CompareUtility.notEquals(GENERAL_CLUSTER_STATUS_PROVIDER_ID, resourceName)) {
      return false;
    }
    //nop
    return true;
  }
}
