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
package org.eclipse.scout.rt.server.services.common.session;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerJobService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ITransactionRunnable;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

public class ServerSessionRegistryService extends AbstractService implements IServerSessionRegistryService {

  private final IServerJobService m_backendService;

  public ServerSessionRegistryService() {
    m_backendService = SERVICES.getService(IServerJobService.class);
  }

  protected IServerJobService getBackendService() {
    return m_backendService;
  }

  @Override
  public <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject) throws ProcessingException {
    return newServerSession(clazz, subject, UserAgent.createDefault());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject, UserAgent userAgent) throws ProcessingException {
    final IServerSession serverSession = createSessionInstance(clazz);
    serverSession.setUserAgent(userAgent);
    final IServerJobFactory jobFactory = getBackendService().createJobFactory(serverSession, subject);
    runLoadSessionJob(serverSession, jobFactory);
    return (T) serverSession;
  }

  protected <T extends IServerSession> IServerSession createSessionInstance(Class<T> clazz) throws ProcessingException {
    IServerSession serverSession;
    try {
      serverSession = clazz.newInstance();
    }
    catch (Throwable t) {
      throw new ProcessingException("create instance of " + clazz, t);
    }
    return serverSession;
  }

  protected void runLoadSessionJob(final IServerSession serverSession, final IServerJobFactory jobFactory) throws ProcessingException {
    jobFactory.runNow("loading session " + serverSession.getClass().getSimpleName(), new ITransactionRunnable() {
      @Override
      public IStatus run(IProgressMonitor monitor) throws ProcessingException {
        String symbolicName = serverSession.getClass().getPackage().getName();
        Bundle bundle = Platform.getBundle(symbolicName);
        serverSession.loadSession(bundle);
        return Status.OK_STATUS;
      }
    });
  }
}
