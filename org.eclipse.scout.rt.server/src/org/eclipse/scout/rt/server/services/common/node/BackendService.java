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

import javax.security.auth.Subject;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerSessionClassFinder;
import org.eclipse.scout.rt.server.commons.cache.ICacheStoreService;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class BackendService implements IBackendService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BackendService.class);
  final String BACKEND_SUBJECT_CACHE = "backendSubjectCache";
  final String BACKEND_SESSION_CACHE = "backendSessionCache";

  @Override
  public Subject getBackendSubject() {
    Subject subject = null;

    subject = (Subject) SERVICES.getService(ICacheStoreService.class).getGlobalAttribute(BACKEND_SUBJECT_CACHE);
    if (subject == null) {
      subject = new Subject();
      subject.getPrincipals().add(new SimplePrincipal("server"));
      SERVICES.getService(ICacheStoreService.class).setGlobalAttribute(BACKEND_SUBJECT_CACHE, subject);
    }

    return subject;
  }

  @Override
  public IServerSession getBackendServerSession() {
    IServerSession session = null;

    try {
      session = (IServerSession) SERVICES.getService(ICacheStoreService.class).getGlobalAttribute(BACKEND_SESSION_CACHE);
      if (session == null) {
        Class<? extends IServerSession> sessionClazz = ServerSessionClassFinder.find();
        Subject subject = new Subject();
        subject.getPrincipals().add(new SimplePrincipal("server"));
        session = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(sessionClazz, getBackendSubject());
        SERVICES.getService(ICacheStoreService.class).setGlobalAttribute(BACKEND_SESSION_CACHE, session);
      }
    }
    catch (Exception e) {
      LOG.error("Unable to get beckend server session", e);
    }

    return session;
  }

  @Override
  public void initializeService(ServiceRegistration registration) {
  }

}
