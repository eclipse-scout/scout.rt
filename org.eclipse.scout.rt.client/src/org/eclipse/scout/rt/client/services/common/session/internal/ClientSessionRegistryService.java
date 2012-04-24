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

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

@Priority(-1)
public class ClientSessionRegistryService extends AbstractService implements IClientSessionRegistryService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientSessionRegistryService.class);

  private final HashMap<String, IClientSession> m_cache = new HashMap<String, IClientSession>();
  private final Object m_cacheLock = new Object();

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IClientSession> T getClientSession(Class<T> clazz, UserAgent userAgent) {
    final Bundle bundle = getDefiningBundle(clazz);
    if (bundle == null) {
      return null;
    }

    synchronized (m_cacheLock) {
      IClientSession clientSession = m_cache.get(bundle.getSymbolicName());
      if (clientSession == null || !clientSession.isActive()) {
        clientSession = createAndStartClientSession(clazz, bundle, userAgent);
        m_cache.put(bundle.getSymbolicName(), clientSession);
      }
      return (T) clientSession;
    }
  }

  @Override
  public <T extends IClientSession> T getClientSession(Class<T> clazz) {
    return getClientSession(clazz, UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN));
  }

  @SuppressWarnings("unchecked")
  private <T extends IClientSession> T createAndStartClientSession(Class<T> clazz, final Bundle bundle, UserAgent userAgent) {
    IClientSession clientSession;
    try {
      clientSession = clazz.newInstance();
      clientSession.setUserAgent(userAgent);
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
      LOG.error("could not load session for " + bundle.getSymbolicName(), t);
      return null;
    }
  }

  @Override
  public <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId, UserAgent userAgent) {
    final Bundle bundle = getDefiningBundle(clazz);
    if (bundle == null) {
      return null;
    }

    return createAndStartClientSession(clazz, bundle, subject, virtualSessionId, userAgent);
  }

  @Override
  public <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId) {
    return newClientSession(clazz, subject, virtualSessionId, UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN));
  }

  @SuppressWarnings("unchecked")
  private <T extends IClientSession> T createAndStartClientSession(Class<T> clazz, final Bundle bundle, Subject subject, String virtualSessionId, UserAgent userAgent) {
    try {
      IClientSession clientSession = clazz.newInstance();
      clientSession.setSubject(subject);
      if (virtualSessionId != null) {
        clientSession.setVirtualSessionId(virtualSessionId);
      }
      clientSession.setUserAgent(userAgent);
      ClientSyncJob job = new ClientSyncJob("Session startup", clientSession) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getCurrentSession().startSession(bundle);
        }
      };
      //must run now to use correct jaas and subject context of calling thread
      job.runNow(new NullProgressMonitor());
      job.throwOnError();
      return (T) clientSession;
    }
    catch (Throwable t) {
      LOG.error("could not load session for " + bundle.getSymbolicName(), t);
      return null;
    }
  }

  private <T extends IClientSession> Bundle getDefiningBundle(Class<T> clazz) {
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

    return Platform.getBundle(symbolicName);
  }

}
