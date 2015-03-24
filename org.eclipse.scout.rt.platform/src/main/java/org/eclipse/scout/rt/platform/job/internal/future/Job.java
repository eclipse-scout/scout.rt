/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Represents a {@link Callable} as well as a {@link Runnable} to be given to the executor for execution. Also, this
 * class contains the {@link IFutureTask} which finally gets scheduled in 'ScheduledThreadPoolExecutor.decorateTask'.
 * <p/>
 * This class is necessary because a {@link ScheduledThreadPoolExecutor} cannot be given a FutureTask as it would be
 * possible with {@link ThreadPoolExecutor}.
 *
 * @since 5.1
 */
@Internal
public class Job<RESULT> implements Callable<RESULT>, Runnable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Job.class);

  private final IFutureTask<RESULT> m_futureTask;
  private final ICallable<RESULT> m_callable;

  public Job(final IFutureTask<RESULT> futureTask, final ICallable<RESULT> callable) {
    m_futureTask = Assertions.assertNotNull(futureTask, "FutureTask must not be null");
    m_callable = Assertions.assertNotNull(callable, "Callable must not be null");
  }

  @Override
  public RESULT call() throws Exception {
    return m_callable.call();
  }

  @Override
  public void run() {
    try {
      m_callable.call();
    }
    catch (final Exception e) {
      LOG.error(String.format("Uncaught exception during job execution [job=%s]", m_futureTask), e);
    }
  }

  /**
   * @return FutureTask used in 'decorateTask' to be given to the executor.
   */
  public IFutureTask<RESULT> getFutureTask() {
    return m_futureTask;
  }

  /**
   * Convenience method to cast the given Job into a {@link Callable}.
   */
  public static <RESULT> Callable<RESULT> callable(final Job<RESULT> job) {
    return job;
  }

  /**
   * Convenience method to cast the given Job into a {@link Runnable}.
   */
  public static Runnable runnable(final Job<?> job) {
    return job;
  }
}
