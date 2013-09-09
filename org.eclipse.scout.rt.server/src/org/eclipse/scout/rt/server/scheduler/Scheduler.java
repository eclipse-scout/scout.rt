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
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.service.SERVICES;

public class Scheduler extends AbstractScheduler implements IScheduler {
  //
  private Subject m_subject;
  private IServerSession m_serverSession;

  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType) throws ProcessingException {
    this(subject, serverSessionType, new Ticker(Calendar.MINUTE));
  }

  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType, Ticker ticker) throws ProcessingException {
    super(ticker);
    m_subject = subject;
    m_serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(serverSessionType, subject);
  }

  @Override
  public void handleJobExecution(final ISchedulerJob job, final TickSignal signal) throws ProcessingException {
    ServerJob serverJob = new ServerJob("Scheduler", m_serverSession, m_subject) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        job.run(Scheduler.this, signal);
        return Status.OK_STATUS;
      }
    };
    serverJob.schedule();
    try {
      serverJob.join();
    }
    catch (InterruptedException ie) {
      throw new ProcessingException("Interrupted", ie);
    }
  }

}
