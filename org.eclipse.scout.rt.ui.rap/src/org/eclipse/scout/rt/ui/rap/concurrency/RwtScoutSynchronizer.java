/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.concurrency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.widgets.Display;

public class RwtScoutSynchronizer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutSynchronizer.class);

  private final IRwtEnvironment m_uiEnvironment;
  private final Display m_rwtQueue;
  //loop detection from rwt to scout
  private LoopDetector m_loopDetector;

  public RwtScoutSynchronizer(IRwtEnvironment uiEnvironment) {
    m_uiEnvironment = uiEnvironment;
    m_rwtQueue = m_uiEnvironment.getDisplay();
    m_loopDetector = new LoopDetector(5000L, 2500, 10);
  }

  private boolean isModelThread() {
    return ClientSyncJob.getCurrentSession() == m_uiEnvironment.getClientSession() && ClientSyncJob.isSyncClientJob();
  }

  public JobEx invokeScoutLater(final Runnable j, long cancelTimeout) {
    if (isModelThread()) {
      LOG.warn("queueing scout runnable into scout thread: " + j);
      j.run();
      return null;
    }
    else if (Thread.currentThread() != m_rwtQueue.getThread()) {
      throw new IllegalStateException("queueing scout runnable from outside rwt thread: " + j);
    }
    //
    m_loopDetector.addSample();
    if (m_loopDetector.isArmed()) {
      LOG.warn("loop detection: " + j, new Exception("Loop detected"));
      return null;
    }
    //send job
    final long deadLine = cancelTimeout > 0 ? System.currentTimeMillis() + cancelTimeout : -1;
    ClientSyncJob eclipseJob = new ClientSyncJob("rwt post::" + j, m_uiEnvironment.getClientSession()) {
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
  public void invokeUiLater(Runnable j) {
    if (m_rwtQueue.isDisposed()) {
      return;
    }
    if (Thread.currentThread() == m_rwtQueue.getThread()) {
      LOG.warn("queueing rwt runnable into rwt thread: " + j);
      j.run();
      return;
    }
    else {
      IClientSession currentSession = ClientSyncJob.getCurrentSession();
      IClientSession uiClientSession = m_uiEnvironment.getClientSession();
      if (currentSession != uiClientSession) {
        String currentUserId = currentSession == null ? "" : currentSession.getUserId();
        String currentWebSessionId = currentSession == null ? "" : currentSession.getVirtualSessionId();
        String uiClientUserId = uiClientSession == null ? "" : uiClientSession.getUserId();
        String uiClientWebSessionId = uiClientSession == null ? "" : uiClientSession.getVirtualSessionId();
        LOG.error("Wrong ClientSession.\n" +
            "CurrentSession:           {0}\tUserId: {2}\tWebSesionId: {3}\n" +
            "EnvironmentClientSession: {4}\tUserId: {5}\tWebSesionId: {5}",
            new Object[]{
                currentSession == null ? "" : currentSession, currentUserId, currentWebSessionId,
                uiClientSession == null ? "" : uiClientSession, uiClientUserId, uiClientWebSessionId});
        throw new IllegalStateException("queueing rwt runnable from outside scout thread: " + j);
      }
    }
    if (!isModelThread()) {
      throw new IllegalStateException("queueing rwt runnable from outside scout thread: " + j);
    }
    //
    m_rwtQueue.asyncExec(j);
  }

}
