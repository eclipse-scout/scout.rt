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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.shared.services.common.session.IJobRunnable;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.service.AbstractService;

/**
 * Default implementation of {@link ISessionService} used on server-side.
 * 
 * @since 3.8.1
 */
@Priority(-1)
public class DefaultServerSessionService extends AbstractService implements ISessionService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultServerSessionService.class);

  @Override
  public IServerSession getCurrentSession() {
    return ServerJob.getCurrentSession();
  }

  @Override
  public JobEx createAsyncJob(IJobRunnable runnable) {
    return createAsyncJob(null, runnable);
  }

  @Override
  public JobEx createAsyncJob(String name, IJobRunnable runnable) {
    IServerSession session = getCurrentSession();
    if (session == null) {
      LOG.error("server session not available");
      return null;
    }
    if (name == null) {
      name = "server session async job";
    }
    return new P_ServerSessionAsyncJob(name, session, runnable);
  }

  private static class P_ServerSessionAsyncJob extends ServerJob {

    private final IJobRunnable m_runnable;

    public P_ServerSessionAsyncJob(String name, IServerSession serverSession, IJobRunnable runnable) {
      super(name, serverSession);
      m_runnable = runnable;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      return m_runnable.run(monitor);
    }
  }
}
