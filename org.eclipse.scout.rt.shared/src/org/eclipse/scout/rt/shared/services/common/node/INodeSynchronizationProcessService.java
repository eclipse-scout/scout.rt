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
package org.eclipse.scout.rt.shared.services.common.node;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 *
 */
public interface INodeSynchronizationProcessService extends IService {

  String getClusterNodeId();

  void publishServerCacheStatus() throws ProcessingException;

  NodeStatusTableHolder getClusterstatusTableData() throws ProcessingException;

  void updateForeignClusterNodeStatus(String originNodeId, List<NodeServiceStatus> statusList) throws ProcessingException;

  void reloadClusterCache(String resourceName) throws ProcessingException;

  List<NodeServiceStatus> getLocalCachesStatus() throws ProcessingException;
}
