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
package org.eclipse.scout.rt.ui.swt.concurrency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.widgets.Display;

public class SwtScoutSynchronizer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutSynchronizer.class);

  private final ISwtEnvironment m_env;
  private final Display m_swtQueue;
  //loop detection from swt to scout
  private LoopDetector m_loopDetector;

  public SwtScoutSynchronizer(ISwtEnvironment env) {
    m_env = env;
    m_swtQueue = m_env.getDisplay();
    m_loopDetector = new LoopDetector(5000L, 2500, 10);
  }

  private boolean isModelThread() {
    return ClientSyncJob.getCurrentSession() == m_env.getClientSession() && ClientSyncJob.isSyncClientJob();
  }

  public JobEx invokeScoutLater(final Runnable j, long cancelTimeout) {
    if (isModelThread()) {
      LOG.warn("queueing scout runnable into scout thread: " + j);
      j.run();
      return null;
    }
    else if (Thread.currentThread() != m_swtQueue.getThread()) {
      throw new IllegalStateException("queueing scout runnable from outside swt thread: " + j);
    }
    //
    m_loopDetector.addSample();
    if (m_loopDetector.isArmed()) {
      LOG.warn("loop detection: " + j, new Exception("Loop detected"));
      return null;
    }
    //send job
    final long deadLine = cancelTimeout > 0 ? System.currentTimeMillis() + cancelTimeout : -1;
    ClientSyncJob eclipseJob = new ClientSyncJob("Swt post::" + j, m_env.getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (deadLine < 0 || deadLine > System.currentTimeMillis()) {
          j.run();
        }
      }
    };
    eclipseJob.schedule();
    return eclipseJob;
  }

  /**
   * calling from scout thread
   */
  public void invokeSwtLater(Runnable j) {
    if (Thread.currentThread() == m_swtQueue.getThread()) {
      LOG.warn("queueing swt runnable into swt thread: " + j);
      j.run();
      return;
    }
    else if (ClientSyncJob.getCurrentSession() != m_env.getClientSession()) {
      throw new IllegalStateException("queueing swt runnable from outside scout thread: " + j);
    }
    if (!isModelThread()) {
      throw new IllegalStateException("queueing swt runnable from outside scout thread: " + j);
    }
    //
    m_swtQueue.asyncExec(j);
  }

}
