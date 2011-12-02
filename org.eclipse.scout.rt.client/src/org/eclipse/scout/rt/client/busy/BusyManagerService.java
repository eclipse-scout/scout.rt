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
package org.eclipse.scout.rt.client.busy;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.client.IJobChangeListenerEx;
import org.eclipse.scout.rt.client.JobChangeAdapterEx;
import org.eclipse.scout.service.AbstractService;

/**
 * The busy manager is the primary place to register/unregister {@link IBusyHandler} per {@link IClientSession}
 * <p>
 * This service is registered by default with priority -1000
 * 
 * @author imo
 * @since 3.8
 */
@Priority(-1000)
public class BusyManagerService extends AbstractService implements IBusyManagerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BusyManagerService.class);
  private static final String HANDLER_CLIENT_SESSION_KEY = IBusyHandler.class.getName();

  private final IJobChangeListener m_jobChangeListener;
  private final IJobChangeListenerEx m_jobChangeListenerEx;

  public BusyManagerService() {
    m_jobChangeListener = new P_JobChangeListener();
    m_jobChangeListenerEx = new P_JobChangeListenerEx();
    Job.getJobManager().addJobChangeListener(m_jobChangeListener);
  }

  private IBusyHandler getHandler(Job job) {
    if (job instanceof IClientSessionProvider) {
      IClientSession session = ((IClientSessionProvider) job).getClientSession();
      if (session != null) {
        return (IBusyHandler) session.getData(HANDLER_CLIENT_SESSION_KEY);
      }
    }
    return null;
  }

  @Override
  public synchronized void register(IClientSession session, IBusyHandler handler) {
    if (session == null || handler == null) {
      return;
    }
    session.setData(HANDLER_CLIENT_SESSION_KEY, handler);
  }

  @Override
  public synchronized void unregister(IClientSession session) {
    if (session == null) {
      return;
    }
    session.setData(HANDLER_CLIENT_SESSION_KEY, null);
  }

  private class P_JobChangeListener extends JobChangeAdapter {
    @Override
    public void running(IJobChangeEvent event) {
      final Job job = event.getJob();
      IBusyHandler handler = getHandler(job);
      if (handler == null) {
        return;
      }
      if (job instanceof ClientJob) {
        ((ClientJob) job).addJobChangeListenerEx(m_jobChangeListenerEx);
      }
      handler.onJobBegin(job);
    }

    @Override
    public void done(IJobChangeEvent event) {
      final Job job = event.getJob();
      IBusyHandler handler = getHandler(job);
      if (handler == null) {
        return;
      }
      if (job instanceof ClientJob) {
        ((ClientJob) job).removeJobChangeListenerEx(m_jobChangeListenerEx);
      }
      handler.onJobEnd(job);
    }
  }

  private class P_JobChangeListenerEx extends JobChangeAdapterEx {
    @Override
    public void blockingConditionStart(IJobChangeEvent event) {
      final Job job = event.getJob();
      IBusyHandler handler = getHandler(job);
      if (handler == null) {
        return;
      }
      handler.onJobEnd(job);
    }

    @Override
    public void blockingConditionEnd(IJobChangeEvent event) {
      final Job job = event.getJob();
      IBusyHandler handler = getHandler(job);
      if (handler == null) {
        return;
      }
      handler.onJobBegin(job);
    }
  }

}
