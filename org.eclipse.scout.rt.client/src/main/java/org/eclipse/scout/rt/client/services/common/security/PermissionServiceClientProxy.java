/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.services.common.security;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelUtility;

/**
 * Cache of available permission types (not instances)
 * <p>
 * Service state is per {@link IClientSession} type and stored in global map
 */
public class PermissionServiceClientProxy implements IPermissionService {

  private final Object m_stateLock = new Object();
  private final Map<Object, ServiceState> m_stateMap = new HashMap<>();

  private ServiceState getServiceState() {
    IClientSession session = ClientSessionProvider.currentSession();
    Object key = null;
    if (session != null) {
      key = session.getClass();
    }
    synchronized (m_stateLock) {
      ServiceState data = m_stateMap.computeIfAbsent(key, k -> new ServiceState());
      return data;
    }
  }

  @Override
  public Set<Class<? extends Permission>> getAllPermissionClasses() {
    ServiceState state = getServiceState();
    checkCache(state);
    return state.m_permissionClasses;
  }

  private void checkCache(ServiceState state) {
    synchronized (state.m_permissionClassesLock) {
      if (state.m_permissionClasses == null) {
        state.m_permissionClasses = getRemoteService().getAllPermissionClasses();
      }
    }
  }

  protected IPermissionService getRemoteService() {
    return ServiceTunnelUtility.createProxy(IPermissionService.class);
  }

  private static final class ServiceState {
    private final Object m_permissionClassesLock = new Object();
    private Set<Class<? extends Permission>> m_permissionClasses;
  }
}
