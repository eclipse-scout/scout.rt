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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.context.ServerRunContext;

public class Scheduler extends AbstractScheduler implements IScheduler {

  private RunContext m_runContext;

  public Scheduler(Ticker ticker, ServerRunContext runContext) throws ProcessingException {
    super(ticker);
    m_runContext = runContext;
  }

  @Override
  public void handleJobExecution(final ISchedulerJob job, final TickSignal signal) throws ProcessingException {
    Jobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        job.run(Scheduler.this, signal);
      }
    }, Jobs.newInput(m_runContext).name(getJobName(job)));
  }

  /**
   * @return name of the {@link ISchedulerJob}.
   */
  protected String getJobName(ISchedulerJob job) {
    return "Scheduler." + job.getGroupId() + "." + job.getJobId();
  }
}
