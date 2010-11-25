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
package org.eclipse.scout.rt.client.services.common.security;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.service.AbstractService;

/**
 * maintains a cache of Permission objects
 */
@Priority(-3)
public class PermissionServiceClientProxy extends AbstractService implements IPermissionService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PermissionServiceClientProxy.class);

  private final Object m_permissionClassesLock = new Object();
  private BundleClassDescriptor[] m_permissionClasses;

  public PermissionServiceClientProxy() {
  }

  public BundleClassDescriptor[] getAllPermissionClasses() {
    checkCache();
    return m_permissionClasses;
  }

  private void checkCache() {
    synchronized (m_permissionClassesLock) {
      if (m_permissionClasses == null) {
        m_permissionClasses = getRemoteService().getAllPermissionClasses();
      }
    }
  }

  private IPermissionService getRemoteService() {
    return ServiceTunnelUtility.createProxy(IPermissionService.class, ClientSyncJob.getCurrentSession().getServiceTunnel());
  }
}
