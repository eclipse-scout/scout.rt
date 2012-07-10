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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

public class ServerSessionRegistryService extends AbstractService implements IServerSessionRegistryService {
  public static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerSessionRegistryService.class);

  @Override
  public <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject) throws ProcessingException {
    return newServerSession(clazz, subject, UserAgent.createDefault());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IServerSession> T newServerSession(Class<T> clazz, Subject subject, UserAgent userAgent) throws ProcessingException {
    IServerSession serverSession;
    try {
      serverSession = clazz.newInstance();
    }
    catch (Throwable t) {
      throw new ProcessingException("create instance of " + clazz, t);
    }
    serverSession.setUserAgent(userAgent);
    ServerJob initJob = new ServerJob("new " + clazz.getSimpleName(), serverSession, subject) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        // load session
        IServerSession serverSessionInside = ThreadContext.getServerSession();
        String symbolicName = serverSessionInside.getClass().getPackage().getName();
        Bundle bundle = Platform.getBundle(symbolicName);
        serverSessionInside.loadSession(bundle);
        return Status.OK_STATUS;
      }
    };
    initJob.runNow(new NullProgressMonitor());
    initJob.throwOnError();
    return (T) serverSession;
  }
}
