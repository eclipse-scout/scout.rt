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
package org.eclipse.scout.rt.client.services.common.session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.services.common.session.IJobRunnable;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.service.AbstractService;

/**
 * Default implementation of {@link ISessionService} used on client-side.
 * 
 * @since 3.8.1
 */
@Priority(-1)
public class DefaultClientSessionService extends AbstractService implements ISessionService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultClientSessionService.class);

  @Override
  public IClientSession getCurrentSession() {
    return ClientJob.getCurrentSession();
  }

  @Override
  public JobEx createAsyncJob(IJobRunnable runnable) {
    return createAsyncJob(null, runnable);
  }

  @Override
  public JobEx createAsyncJob(String name, IJobRunnable runnable) {
    IClientSession session = getCurrentSession();
    if (session == null) {
      LOG.error("client session not available");
      return null;
    }
    if (name == null) {
      name = "client session async job";
    }
    return new P_ClientSessionAsyncJob(name, session, true, runnable);
  }

  private static class P_ClientSessionAsyncJob extends ClientAsyncJob {

    private final IJobRunnable m_runnable;

    public P_ClientSessionAsyncJob(String name, IClientSession session, boolean system, IJobRunnable runnable) {
      super(name, session, system);
      m_runnable = runnable;
    }

    @Override
    protected IStatus runStatus(IProgressMonitor monitor) {
      return m_runnable.run(monitor);
    }
  }
}
