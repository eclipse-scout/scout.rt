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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

public class Scheduler extends AbstractScheduler implements IScheduler {

  private ServerJobInput m_jobInput;

  public Scheduler() throws ProcessingException {
    this(new Ticker(Calendar.MINUTE), ServerJobInput.defaults().sessionRequired(false).transactional(false));
  }

  @Deprecated
  // TODO [dwi] [session]: remove me until session class is registered in platform.
  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType) throws ProcessingException {
    this(subject, serverSessionType, new Ticker(Calendar.MINUTE));
  }

  @Deprecated
  // TODO [dwi] [session]: remove me until session class is registered in platform.
  public Scheduler(Subject subject, Class<? extends IServerSession> serverSessionType, Ticker ticker) throws ProcessingException {
    this(ticker, ServerJobInput.empty().subject(subject).session(loadServerSession(serverSessionType, subject)));
  }

  public Scheduler(Ticker ticker, ServerJobInput jobInput) throws ProcessingException {
    super(ticker);
    m_jobInput = jobInput;
  }

  @Override
  public void handleJobExecution(final ISchedulerJob job, final TickSignal signal) throws ProcessingException {
    OBJ.one(IServerJobManager.class).runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        job.run(Scheduler.this, signal);
      }
    }, m_jobInput.copy().name(getJobName(job)));
  }

  /**
   * @return name of the {@link ISchedulerJob}.
   */
  protected String getJobName(ISchedulerJob job) {
    return "Scheduler." + job.getGroupId() + "." + job.getJobId();
  }

  // TODO [dwi] [session]: remove temporary workaround until session class is registered in platform.
  @Deprecated
  private static IServerSession loadServerSession(Class<? extends IServerSession> serverSessionType, Subject subject) throws ProcessingException {
    IServerSession currentServerSession = OBJ.oneOrNull(IServerSession.class);
    if (currentServerSession != null) {
      IBean<?> oldServerSessionBean = OBJ.registerClass(currentServerSession.getClass());
      OBJ.unregisterBean(oldServerSessionBean);
      try {
        return loadServerSessionInternal(serverSessionType, subject);
      }
      finally {
        OBJ.registerBean(oldServerSessionBean, null);
      }
    }
    else {
      return loadServerSessionInternal(serverSessionType, subject);
    }
  }

  // TODO [dwi] [session]: remove temporary workaround until session class is registered in platform.
  @Deprecated
  private static IServerSession loadServerSessionInternal(Class<? extends IServerSession> serverSessionType, Subject subject) throws ProcessingException {
    IBean<?> newServerSessionBean = OBJ.registerClass(serverSessionType);
    try {
      return OBJ.one(ServerSessionProviderWithCache.class).provide(ServerJobInput.empty().subject(subject));
    }
    finally {
      OBJ.unregisterBean(newServerSessionBean);
    }
  }
}
