/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.scheduler;

import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContext;

public class Scheduler extends AbstractScheduler implements IScheduler {

  private ServerRunContext m_serverRunContext;

  public Scheduler(Ticker ticker, ServerRunContext serverRunContext) {
    super(ticker);
    m_serverRunContext = serverRunContext;
  }

  @Override
  public void handleJobExecution(final ISchedulerJob job, final TickSignal signal) {
    m_serverRunContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        job.run(Scheduler.this, signal);
      }
    });
  }
}
