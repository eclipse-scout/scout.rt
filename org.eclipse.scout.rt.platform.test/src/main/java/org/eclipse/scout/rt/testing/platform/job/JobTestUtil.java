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
package org.eclipse.scout.rt.testing.platform.job;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.JobManager;

/**
 * Helper class to run job related JUnit tests.
 */
public class JobTestUtil {

  /**
   * Registers a dedicated {@link IJobManager}.
   */
  public static IBean<IJobManager> registerJobManager() {
    return registerJobManager(new JobManager());
  }

  /**
   * Registers the given job manager as dedicated {@link IJobManager}.
   */
  public static IBean<IJobManager> registerJobManager(final IJobManager jobManager) {
    final IBean<IJobManager> jobManagerBean = Platform.get().getBeanManager().registerBean(new BeanMetaData(JobManager.class, jobManager).withReplace(true).withOrder(-1));
    Assertions.assertSame(jobManager, BEANS.get(IJobManager.class));
    return jobManagerBean;
  }

  /**
   * Unregisters the given job manager.
   */
  public static void unregisterJobManager(final IBean<?> jobManagerBean) {
    Jobs.getJobManager().shutdown();
    Platform.get().getBeanManager().unregisterBean(jobManagerBean);
  }

  /**
   * Blocks the calling thread until the expected number of tasks competing for the mutex is reached. That is the
   * mutex-owner plus any queued task. This method blocks 30s at maximum, and throws an {@link AssertionError} if
   * elapsed.
   */
  public static void waitForMutexCompetitors(final IMutex mutex, final int expectedCompetitorCount) throws InterruptedException {
    final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    while (mutex.getCompetitorCount() != expectedCompetitorCount) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for a mutex-permit count. [expectedPermitCount=%s, actualPermitCount=%s]", expectedCompetitorCount, mutex.getCompetitorCount()));
      }
      Thread.sleep(10);
    }
  }
}
