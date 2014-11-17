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
package org.eclipse.scout.rt.server.scheduler;

import java.util.Calendar;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerJobService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ITransactionRunnable;
import org.eclipse.scout.service.SERVICES;

public class Scheduler extends AbstractScheduler implements IScheduler {
  private final IServerJobFactory m_serverJobFactory;

  public Scheduler() throws ProcessingException {
    super(new Ticker(Calendar.MINUTE));
    final IServerJobService backendService = SERVICES.getService(IServerJobService.class);
    m_serverJobFactory = backendService.createJobFactory();
  }

  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType) throws ProcessingException {
    this(subject, serverSessionType, new Ticker(Calendar.MINUTE));
  }

  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType, Ticker ticker) throws ProcessingException {
    super(ticker);
    final IServerJobService backendService = SERVICES.getService(IServerJobService.class);
    m_serverJobFactory = backendService.createJobFactory(backendService.createServerSession(serverSessionType, subject), subject);
  }

  @Override
  public void handleJobExecution(final ISchedulerJob job, final TickSignal signal) throws ProcessingException {
    final String jobName = "Scheduler." + job.getGroupId() + "." + job.getJobId();
    m_serverJobFactory.runNow(jobName, new ITransactionRunnable() {

      @Override
      public IStatus run(IProgressMonitor monitor) throws ProcessingException {
        job.run(Scheduler.this, signal);
        return Status.OK_STATUS;
      }
    });
  }
}
