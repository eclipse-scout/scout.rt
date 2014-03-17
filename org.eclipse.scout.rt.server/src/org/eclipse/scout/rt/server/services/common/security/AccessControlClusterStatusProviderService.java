package org.eclipse.scout.rt.server.services.common.security;

import java.util.Date;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.node.INodeSynchronizationStatusProviderService;
import org.eclipse.scout.rt.server.services.common.security.internal.INodeSynchronizationAccessControlService;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

public class AccessControlClusterStatusProviderService extends AbstractService implements INodeSynchronizationStatusProviderService {

  private static final String CLUSTER_STATUS_PROVIDER_ID = "AccessControlStore";

  @Override
  public NodeServiceStatus getClusterStatus() throws ProcessingException {
    INodeSynchronizationAccessControlService service = SERVICES.getService(INodeSynchronizationAccessControlService.class);
    NodeServiceStatus serviceStatus = new NodeServiceStatus();
    serviceStatus.setServiceName(CLUSTER_STATUS_PROVIDER_ID);
    serviceStatus.setDescription(TEXTS.get("AccessControlStoreDescription"));
    serviceStatus.setReloadClusterCacheMenuText(TEXTS.get("ResetRolesOfAllUsers"));
    serviceStatus.setStatusDate(new Date());
    service.fillClusterServiceStatus(serviceStatus);
    return serviceStatus;
  }

  @Override
  public boolean reloadClusterCache(String resourceName) throws ProcessingException {
    if (CompareUtility.notEquals(CLUSTER_STATUS_PROVIDER_ID, resourceName)) {
      return false;
    }
    SERVICES.getService(IAccessControlService.class).clearCache();
    return true;
  }
}
