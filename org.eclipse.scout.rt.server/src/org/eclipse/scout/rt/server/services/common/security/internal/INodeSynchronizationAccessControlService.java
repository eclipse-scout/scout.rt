package org.eclipse.scout.rt.server.services.common.security.internal;

import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.IService;

/**
 *
 */
public interface INodeSynchronizationAccessControlService extends IAccessControlService, IService {
  /**
   * @param changedUserId
   * @param changedClusterNodeId
   */
  void clearCacheInternal(String changedUserId, String changedClusterNodeId);

  /**
   * @param changedUserId
   * @param changedClusterNodeId
   * @param userIds
   */
  void clearCacheOfUserIdsInternal(String changedUserId, String changedClusterNodeId, String... userIds);

  /**
   * @param serviceStatus
   *          not <code>null</code>
   */
  void fillClusterServiceStatus(NodeServiceStatus serviceStatus);
}
