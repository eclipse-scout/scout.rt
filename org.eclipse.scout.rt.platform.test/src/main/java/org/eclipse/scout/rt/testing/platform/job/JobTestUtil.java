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
package org.eclipse.scout.rt.testing.platform.job;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.JobManager;

/**
 * Helper class for Job API in JUnit tests.
 */
public class JobTestUtil {

  /**
   * Replaces the current {@link JobManager} with the given job manager instance.
   *
   * @return the registration to be used to restore to the current job manager.
   */
  public static IBean<IJobManager> replaceCurrentJobManager(final JobManager jobManager) {
    final IBean<IJobManager> jobManagerBean = BEANS.getBeanManager().registerBean(new BeanMetaData(jobManager.getClass(), jobManager).withReplace(true));
    assertSame("Unexpected: wrong job manager registered", jobManager, BEANS.get(IJobManager.class));

    return jobManagerBean;
  }

  public static void unregisterAndShutdownJobManager(final IBean<IJobManager> jobManagerBean) {
    final IJobManager jobManager = jobManagerBean.getInstance();
    BEANS.getBeanManager().unregisterBean(jobManagerBean);
    jobManager.shutdown();
  }

  /**
   * Waits if necessary for at most 30s until the {@link IExecutionSemaphore} reaches the expected competitor count. If
   * elapsed, an {@link AssertionError} is thrown.
   * <p>
   * Competitor count: all permit owners plus all queued task.
   */
  public static void waitForPermitCompetitors(final IExecutionSemaphore semaphore, final int expectedCompetitorCount) {
    waitForCondition(new ICondition() {

      @Override
      public boolean isFulfilled() {
        return semaphore.getCompetitorCount() == expectedCompetitorCount;
      }

      @Override
      public String toString() {
        return String.format("expectedPermitCount=%s, actualPermitCount=%s", expectedCompetitorCount, semaphore.getCompetitorCount());
      }
    });
  }

  /**
   * Waits if necessary for at most 30s until the {@link IFuture} enters the expected state. If elapsed, an
   * {@link AssertionError} is thrown.
   * <p>
   * <strong>When this method returns, the future is in the given state. But, event listeners may not necessarily
   * notified yet.</strong>
   */
  public static void waitForState(final IFuture<?> future, final JobState expectedState) {
    waitForCondition(new ICondition() {

      @Override
      public boolean isFulfilled() {
        return future.getState() == expectedState;
      }

      @Override
      public String toString() {
        return String.format("expectedState=%s, actualState=%s", expectedState, future.getState());
      }
    });
  }

  /**
   * Waits if necessary for at most 30s until the condition is fulfilled. If elapsed, an {@link AssertionError} is
   * thrown.
   */
  public static void waitForCondition(final ICondition condition) {
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (!condition.isFulfilled()) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for a condition to be fulfilled. [condition=%s]", condition));
      }
      Thread.yield();
    }
  }

  public static interface ICondition {

    boolean isFulfilled();
  }
}
