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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.notification.INotificationService;
import org.eclipse.scout.rt.shared.services.common.node.INodeSynchronizationProcessService;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.rt.shared.services.common.node.NodeStatusTableHolder;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

public class NodeSynchronizationProcessService extends AbstractService implements INodeSynchronizationProcessService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NodeSynchronizationProcessService.class);
  private final Map<String, List<NodeServiceStatus>> m_foreignClusterNodesStatusMap = new HashMap<String, List<NodeServiceStatus>>();
  private final Object m_cacheLock = new Object();

  private String m_nodeId;

  public String getNodeId() {
    return m_nodeId;
  }

  public void setNodeId(String nodeId) {
    m_nodeId = nodeId;
  }

  @Override
  public String getClusterNodeId() {
    // config defined node id
    String originNodeId = getNodeId();

    // default is to generate a random node id
    if (!StringUtility.hasText(originNodeId)) {
      synchronized (m_cacheLock) {
        setNodeId(UUID.randomUUID().toString());
        originNodeId = getNodeId();
      }
    }
    return originNodeId;
  }

  @Override
  public void publishServerCacheStatus() throws ProcessingException {
    SERVICES.getService(INotificationService.class).publishNotification(new RequestServerStatusNotification(getLocalCachesStatus()));
  }

  @Override
  public void updateForeignClusterNodeStatus(String originNodeId, List<NodeServiceStatus> statusList) throws ProcessingException {
    synchronized (m_cacheLock) {
      m_foreignClusterNodesStatusMap.put(originNodeId, statusList);
    }
  }

  @Override
  public void reloadClusterCache(String resourceName) throws ProcessingException {
    if (!StringUtility.hasText(resourceName)) {
      throw new ProcessingException("resourceName is null");
    }
    INodeSynchronizationStatusProviderService[] services = SERVICES.getServices(INodeSynchronizationStatusProviderService.class);
    if (services != null) {
      for (INodeSynchronizationStatusProviderService statusProvider : services) {
        if (statusProvider.reloadClusterCache(resourceName)) {
          return;
        }
      }
    }
    throw new ProcessingException("unsupported resourceName '" + resourceName + "'");
  }

  @Override
  public NodeStatusTableHolder getClusterstatusTableData() throws ProcessingException {
    Map<String, Map<String, NodeServiceStatus>> statusNodeMap = new HashMap<String, Map<String, NodeServiceStatus>>();
    Set<String> nodeIds = new HashSet<String>();

    // local information
    for (NodeServiceStatus status : getLocalCachesStatus()) {
      nodeIds.add(status.getOriginNodeId());
      Map<String, NodeServiceStatus> nodeMap = statusNodeMap.get(status.getServiceName());
      if (nodeMap == null) {
        nodeMap = new HashMap<String, NodeServiceStatus>();
        statusNodeMap.put(status.getServiceName(), nodeMap);
      }
      nodeMap.put(status.getOriginNodeId(), status);
    }

    //Request cache information from all other nodes
    SERVICES.getService(INotificationService.class).publishNotification(new RequestServerCacheStatusNotification());
    try {
      // wait one second for updates
      Thread.sleep(2000L);
    }
    catch (InterruptedException e) {
      LOG.error("unable to sleep",e);
    }

    synchronized (m_cacheLock) {
      for (Map.Entry<String, List<NodeServiceStatus>> foreignNode : m_foreignClusterNodesStatusMap.entrySet()) {
        nodeIds.add(foreignNode.getKey());
        for (NodeServiceStatus status : foreignNode.getValue()) {
          Map<String, NodeServiceStatus> nodeMap = statusNodeMap.get(status.getServiceName());
          if (nodeMap == null) {
            nodeMap = new HashMap<String, NodeServiceStatus>();
            statusNodeMap.put(status.getServiceName(), nodeMap);
          }
          nodeMap.put(status.getOriginNodeId(), status);
        }
      }
    }

    // create and populate resulting bean
    NodeStatusTableHolder result = new NodeStatusTableHolder();
    String localClusterNodeId = SERVICES.getService(INodeSynchronizationProcessService.class).getClusterNodeId();
    result.setProcessingNode(localClusterNodeId);

    String[] sortedNodeIds = nodeIds.toArray(new String[nodeIds.size()]);
    Arrays.sort(sortedNodeIds);
    result.setTableHeaders(sortedNodeIds);

    List<Object[]> data = new ArrayList<Object[]>();

    for (Map.Entry<String, Map<String, NodeServiceStatus>> entry : statusNodeMap.entrySet()) {
      List<Object> row = new ArrayList<Object>();
      Map<String, NodeServiceStatus> nodesStatus = entry.getValue();
      NodeServiceStatus localStatus = nodesStatus.get(localClusterNodeId);
      row.add(entry.getKey());
      if (localStatus != null) {
        row.add(localStatus.getDescription());
        row.add(localStatus.getReloadClusterCacheMenuText());
      }
      else {
        row.add(null);
        row.add(null);
      }
      for (String nodeId : sortedNodeIds) {
        NodeServiceStatus status = nodesStatus.get(nodeId);
        row.add(formatStatus(status));
      }
      data.add(row.toArray(new Object[row.size()]));
    }
    result.setTableData(data.toArray(new Object[data.size()][]));
    return result;
  }

  @Override
  public List<NodeServiceStatus> getLocalCachesStatus() throws ProcessingException {
    List<NodeServiceStatus> result = new ArrayList<NodeServiceStatus>();
    INodeSynchronizationStatusProviderService[] services = SERVICES.getServices(INodeSynchronizationStatusProviderService.class);
    if (services != null) {
      for (INodeSynchronizationStatusProviderService provider : services) {
        NodeServiceStatus status = provider.getClusterStatus();
        if (status == null || !StringUtility.hasText(status.getServiceName())) {
          continue;
        }
        status.setOriginNodeId(getClusterNodeId());
        result.add(status);
      }
    }
    return result;
  }

  private String formatStatus(NodeServiceStatus status) throws ProcessingException {
    if (status == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    sb.append("Status gathered at: <b>");
    sb.append(DateUtility.format(status.getStatusDate(), "dd.MM.yyyy HH:mm:ss.SSS"));

//    if (CompareUtility.equals(status.getServiceName(), GeneralClusterStatusProviderService.GENERAL_CLUSTER_STATUS_PROVIDER_ID)) {
//      sb.append("</b><br/><br/>Last message: <b>");
//      sb.append("</b><br/>Received at: <b>");
//      sb.append(textOrDash(DateUtility.format(status.getLastChangedDate(), "dd.MM.yyyy HH:mm:ss")));
//      sb.append("</b><br/>Received by: <b>");
//      sb.append(textOrDash(status.getLastChangedUserId()));
//      sb.append("</b><br/>Received from node: <b>");
//      sb.append(textOrDash(status.getLastChangedClusterNodeId()));
//    }
//    else {
    sb.append("</b><br/><br/>Changed at: <b>");
    sb.append(textOrDash(DateUtility.format(status.getLastChangedDate(), "dd.MM.yyyy HH:mm:ss")));
    sb.append("</b><br/>Changed by: <b>");
    sb.append(textOrDash(status.getLastChangedUserId()));
    sb.append("</b><br/>Changed on node: <b>");
    sb.append(textOrDash(status.getLastChangedClusterNodeId()));
//    }

    sb.append("</b><br/>");
    for (String line : status.getInfoLines()) {
      sb.append("<br/>");
      sb.append(line);
    }
    sb.append("<br/>&nbsp;</html>");
    return sb.toString();
  }

  public static String textOrDash(String str) {
    return (str == null || "".equals(str.trim())) ? "-" : str;
  }
}
