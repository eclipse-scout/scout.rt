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
import org.eclipse.scout.rt.platform.job.ISchedulingSemaphore;
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
   * Blocks the calling thread until the expected number of tasks competing for a permit is reached. That is all the
   * permit owners plus all queued task. This method blocks 30s at maximum, and throws an {@link AssertionError} if
   * elapsed.
   */
  public static void waitForPermitCompetitors(final ISchedulingSemaphore semaphore, final int expectedCompetitorCount) {
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (semaphore.getCompetitorCount() != expectedCompetitorCount) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for a semaphore-permit count. [expectedPermitCount=%s, actualPermitCount=%s]", expectedCompetitorCount, semaphore.getCompetitorCount()));
      }
      Thread.yield();
    }
  }

  /**
   * Blocks the calling thread until the job enters the given state. This method blocks 30s at maximum, and throws an
   * {@link AssertionError} if elapsed.
   * <p>
   * <strong>When this method returns, the future is in the given state. But, event listeners not necessarily notified
   * yet.</strong>
   */
  public static void waitForState(final IFuture<?> future, final JobState state) {
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (future.getState() != state) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for a job to enter a state. [expectedState=%s, currentState=%s]", state, future.getState()));
      }
      Thread.yield();
    }
  }
}
