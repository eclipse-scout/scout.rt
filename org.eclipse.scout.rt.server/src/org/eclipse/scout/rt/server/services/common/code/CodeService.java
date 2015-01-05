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
package org.eclipse.scout.rt.server.services.common.code;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.services.common.clientnotification.AllUserFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListenerService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.SharedCodeService;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.SERVICES;

/**
 * delegates to {@link CodeTypeStore}
 */
@Priority(-1)
public class CodeService extends SharedCodeService implements IClusterNotificationListenerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeService.class);

  @Override
  protected Long provideCurrentPartitionId() {
    Map<String, Object> sharedVariableMap = ServerJob.getCurrentSession().getSharedVariableMap();
    if (sharedVariableMap.containsKey(ICodeType.PROP_PARTITION_ID)) {
      return (Long) sharedVariableMap.get(ICodeType.PROP_PARTITION_ID);
    }
    return super.provideCurrentPartitionId();
  }

  @Override
  protected void notifyReloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> codetypeList) throws ProcessingException {
    // notify clients:
    SERVICES.getService(IClientNotificationService.class).putNotification(new CodeTypeChangedNotification(codetypeList), new AllUserFilter(AllUserFilter.DEFAULT_TIMEOUT));

    // notify clusters:
    IClusterSynchronizationService s = SERVICES.getService(IClusterSynchronizationService.class);
    if (s != null) {
      s.publishNotification(new UnloadCodeTypeCacheClusterNotification(codetypeList));
    }
  }

  @Override
  public IClusterNotificationListener getClusterNotificationListener() {
    return new IClusterNotificationListener() {
      @Override
      public void onNotification(IClusterNotificationMessage message) throws ProcessingException {
        IClusterNotification clusterNotification = message.getNotification();
        if (clusterNotification instanceof UnloadCodeTypeCacheClusterNotification) {
          UnloadCodeTypeCacheClusterNotification n = (UnloadCodeTypeCacheClusterNotification) clusterNotification;
          reloadCodeTypesNoFire(n.getTypes());
        }
      }
    };
  }

  @Override
  public Class<? extends IService> getDefiningServiceInterface() {
    return ICodeService.class;
  }
}
