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
package org.eclipse.scout.rt.ui.swing.concurrency;

import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ModelJobInput;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;

public class SwingScoutSynchronizer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutSynchronizer.class);

  private final ISwingEnvironment m_env;
  //loop detection from swing to scout
  private final LoopDetector m_loopDetector;

  public SwingScoutSynchronizer(ISwingEnvironment env) {
    m_env = env;
    m_loopDetector = new LoopDetector(5000L, 2500, 10);
  }

  /**
   * Calling from swing thread and posting scout job to complete.
   * <p>
   * The job is only run, when it is processed before timeout (value > 0), otherwise it is ignored.
   * <p>
   * A timeout value &lt;= 0 means no timeout.
   */
  public IFuture<Void> invokeScoutLater(final Runnable j, long cancelTimeout) {
    if (ModelJobs.isModelThread()) {
      LOG.warn("trying to queue scout runnable but already in scout thread: " + j);
    }
    else if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("queueing scout runnable from outside swing thread: " + j);
    }

    // do loop detection
    m_loopDetector.addSample();
    if (m_loopDetector.isArmed()) {
      LOG.error("loop detection: " + j, new Exception("Loop detected"));
      return null;
    }

    // schedule job
    final long deadLine = cancelTimeout > 0 ? System.currentTimeMillis() + cancelTimeout : -1;
    IFuture<Void> future = ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        if (deadLine < 0 || deadLine > System.currentTimeMillis()) {
          j.run();
        }
      }
    }, ModelJobInput.fillCurrent().session(m_env.getScoutSession()).name("Swing post::" + j));
    return future;
  }

  /**
   * calling from scout thread and waiting for swing job to complete
   */
  public void invokeSwingLater(final Runnable j) {
    if (SwingUtilities.isEventDispatchThread()) {
      LOG.warn("trying to queue swing runnable but already in swing thread: " + j);
    }
    if (!ModelJobs.isModelThread()) {
      throw new IllegalStateException("queueing swing runnable from outside scout thread: " + j);
    }
    SwingUtilities.invokeLater(j);
  }
}
