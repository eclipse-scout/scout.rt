package org.eclipse.scout.rt.server.services.common.code;

import java.util.Date;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.node.INodeSynchronizationStatusProviderService;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.node.NodeServiceStatus;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

public class CodeTypeClusterStatusProviderService extends AbstractService implements INodeSynchronizationStatusProviderService {

  private static final String CLUSTER_STATUS_PROVIDER_ID = "AccessControlStore";

  @Override
  public NodeServiceStatus getClusterStatus() throws ProcessingException {
    INodeSynchronizationCodeTypeService service = SERVICES.getService(INodeSynchronizationCodeTypeService.class);
    NodeServiceStatus serviceStatus = new NodeServiceStatus();
    serviceStatus.setServiceName(CLUSTER_STATUS_PROVIDER_ID);
    //TODO tsw prüfen wohin die Texte müssen!
    serviceStatus.setDescription(TEXTS.get("CodeTypeStoreDescription"));
    //serviceStatus.setReloadClusterCacheMenuText(TEXTS.get("ResetRolesOfAllUsers"));
    serviceStatus.setStatusDate(new Date());
    service.fillClusterServiceStatus(serviceStatus);
    return serviceStatus;
  }

  @Override
  public boolean reloadClusterCache(String resourceName) throws ProcessingException {
    return true;
  }
}
