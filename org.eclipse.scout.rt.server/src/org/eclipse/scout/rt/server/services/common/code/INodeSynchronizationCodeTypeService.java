package org.eclipse.scout.rt.server.services.common.code;

import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.service.IService;

/**
 *
 */
public interface INodeSynchronizationCodeTypeService extends IService {

  /**
   * @param serviceStatus
   *          not <code>null</code>
   */
  void fillClusterServiceStatus(NodeServiceStatus serviceStatus);
}
