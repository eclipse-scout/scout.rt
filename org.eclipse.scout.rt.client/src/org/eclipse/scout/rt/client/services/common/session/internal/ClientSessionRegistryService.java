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
package org.eclipse.scout.rt.client.services.common.session.internal;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

@Priority(-1)
public class ClientSessionRegistryService extends AbstractService implements IClientSessionRegistryService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientSessionRegistryService.class);

  private final HashMap<String, IClientSession> m_cache = new HashMap<String, IClientSession>();
  private final Object m_cacheLock = new Object();

  @SuppressWarnings("unchecked")
  public <T extends IClientSession> T getClientSession(Class<T> clazz) {
    String symbolicName = clazz.getPackage().getName();
    Bundle bundleLocator = null;
    while (symbolicName != null) {
      bundleLocator = Platform.getBundle(symbolicName);
      int i = symbolicName.lastIndexOf('.');
      if (bundleLocator != null || i < 0) {
        break;
      }
      symbolicName = symbolicName.substring(0, i);
    }
    final Bundle bundle = Platform.getBundle(symbolicName);
    if (bundle != null) {
      synchronized (m_cacheLock) {
        IClientSession clientSession = m_cache.get(bundle.getSymbolicName());
        if (clientSession == null || !clientSession.isActive()) {
          try {
            clientSession = clazz.newInstance();
            m_cache.put(symbolicName, clientSession);
            ClientSyncJob job = new ClientSyncJob("Session startup", clientSession) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                getCurrentSession().startSession(bundle);
              }
            };
            job.schedule();
            job.join();
            job.throwOnError();
          }
          catch (Throwable t) {
            LOG.error("could not load session for " + symbolicName, t);
          }
        }
        return (T) clientSession;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends IClientSession> T newClientSession(Class<T> clazz, String webSessionId) {
    String symbolicName = clazz.getPackage().getName();
    Bundle bundleLocator = null;
    while (symbolicName != null) {
      bundleLocator = Platform.getBundle(symbolicName);
      int i = symbolicName.lastIndexOf('.');
      if (bundleLocator != null || i < 0) {
        break;
      }
      symbolicName = symbolicName.substring(0, i);
    }
    final Bundle bundle = Platform.getBundle(symbolicName);
    if (bundle != null) {
      try {
        IClientSession clientSession = clazz.newInstance();
        if (webSessionId != null) {
          clientSession.setWebSessionId(webSessionId);
        }
        ClientSyncJob job = new ClientSyncJob("Session startup", clientSession) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            getCurrentSession().startSession(bundle);
          }
        };
        job.schedule();
        job.join();
        job.throwOnError();
        return (T) clientSession;
      }
      catch (Throwable t) {
        LOG.error("could not load session for " + symbolicName, t);
      }
    }
    return null;
  }
}
