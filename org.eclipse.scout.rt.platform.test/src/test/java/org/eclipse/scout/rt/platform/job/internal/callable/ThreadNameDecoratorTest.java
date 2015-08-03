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
package org.eclipse.scout.rt.platform.job.internal.callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  private IJobManager m_jobManager;
  private IBean<Object> m_bean;

  @Before
  public void before() {
    m_jobManager = new JobManager();
    m_bean = Platform.get().getBeanManager().registerBean(new BeanMetaData(JobManager.class, m_jobManager).withReplace(true).withOrder(-1));
  }

  @After
  public void after() {
    m_jobManager.shutdown();
    Platform.get().getBeanManager().unregisterBean(m_bean);
  }

  @Test
  public void testThreadName() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    ThreadInfo.CURRENT.set(new ThreadInfo(Thread.currentThread(), "scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", "123:job1").call();
    assertEquals("scout-client-thread-5 (Running) \"123:job1\"", threadName.getValue());
    assertEquals("scout-thread-5 (Idle)", Thread.currentThread().getName());
    ThreadInfo.CURRENT.remove();
  }

  @Test
  public void testThreadNameWithEmptyJobIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    ThreadInfo.CURRENT.set(new ThreadInfo(Thread.currentThread(), "scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", null).call();
    assertEquals("scout-client-thread-5 (Running)", threadName.getValue());
    assertEquals("scout-thread-5 (Idle)", Thread.currentThread().getName());
    ThreadInfo.CURRENT.remove();
  }

  @Test
  @Times(50)
  // This test is executed 50 times (regression)
  public void testThreadNameBlocking() throws Exception {
    final Object mutexObject = new Object();
    final IBlockingCondition BC = Jobs.getJobManager().createBlockingCondition("blocking-condition", true);

    final Holder<Thread> workerThreadJob1Holder = new Holder<>();
    final Holder<ThreadInfo> threadInfoJob1Holder = new Holder<>();

    // Job-1 (same mutex as job-2)
    IFuture<Boolean> future1 = Jobs.schedule(new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        workerThreadJob1Holder.setValue(Thread.currentThread());
        threadInfoJob1Holder.setValue(ThreadInfo.CURRENT.get());

        // verify job-status is 'RUNNING'.
        String currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\(Running\\) \"job-1\""));

        // Start blocking
        BC.waitFor();

        // Wait for the thread name to be updated because job-state change events are fired asynchronously (max 30s).
        waitForConditionElseFail(new ICondition() {

          @Override
          public boolean evaluate() {
            return JobState.Running.equals(ThreadInfo.CURRENT.get().getCurrentJobState());
          }
        });

        // verify job-status is 'RUNNING'.
        currentThreadName = Thread.currentThread().getName();
        assertTrue("actual=" + currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\(Running\\) \"job-1\""));
        return true;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()).withName("job-1").withMutex(mutexObject));

    // Job-2 (same mutex as job-1)
    IFuture<Boolean> future2 = Jobs.schedule(new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        // Wait for the thread name to be updated because job-state change events are fired asynchronously (max 30s).
        waitForConditionElseFail(new ICondition() {

          @Override
          public boolean evaluate() {
            return JobState.Blocked.equals(threadInfoJob1Holder.getValue().getCurrentJobState());
          }
        });

        // verify job1 to be in blocked state.
        String threadNameJob1 = workerThreadJob1Holder.getValue().getName();
        assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(Blocked 'blocking-condition'\\) \"job-1\""));

        // Release job-1
        BC.setBlocking(false);

        // Wait for the thread name to be updated because job-state change events are fired asynchronously (max 30s).
        waitForConditionElseFail(new ICondition() {

          @Override
          public boolean evaluate() {
            return JobState.Resuming.equals(threadInfoJob1Holder.getValue().getCurrentJobState());
          }
        });

        // verify job1 to be in resume state.
        threadNameJob1 = workerThreadJob1Holder.getValue().getName();
        assertTrue("actual=" + threadNameJob1, threadNameJob1.matches("scout-thread-\\d+ \\(Resuming 'blocking-condition'\\) \"job-1\""));

        return true;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()).withName("job-1").withMutex(mutexObject));

    assertTrue(future2.awaitDoneAndGet());
    assertTrue(future1.awaitDoneAndGet());
  }

  private static void waitForConditionElseFail(ICondition condition) throws InterruptedException {
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

    // Wait until the other jobs tried to re-acquire the mutex.
    while (!condition.evaluate()) {
      if (System.currentTimeMillis() > deadline) {
        fail(String.format("Timeout elapsed while waiting for the condition to be met. [condition=%s]", condition));
      }
      Thread.sleep(10);
    }
  }

  private interface ICondition {
    boolean evaluate();
  }
}
