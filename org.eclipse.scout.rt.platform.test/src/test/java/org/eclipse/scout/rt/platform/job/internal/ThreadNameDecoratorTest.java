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
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  @Test
  public void testThreadName() throws Exception {
    final StringHolder threadName = new StringHolder();

    ThreadInfo.CURRENT.set(new ThreadInfo(Thread.currentThread(), "scout-thread", 5));
    IFuture.CURRENT.set(mockFuture());

    CallableChain<Void> callableChain = new CallableChain<Void>();
    callableChain.add(new ThreadNameDecorator("scout-client-thread", "123:job1"));
    callableChain.call(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    });

    assertEquals("scout-client-thread-5 (Running) \"123:job1\"", threadName.getValue());
    assertEquals("scout-thread-5 (Idle)", Thread.currentThread().getName());

    ThreadInfo.CURRENT.remove();
    IFuture.CURRENT.remove();
  }

  @Test
  public void testThreadNameWithEmptyJobIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    ThreadInfo.CURRENT.set(new ThreadInfo(Thread.currentThread(), "scout-thread", 5));
    IFuture.CURRENT.set(mockFuture());

    CallableChain<Void> callableChain = new CallableChain<Void>();
    callableChain.add(new ThreadNameDecorator("scout-client-thread", null));
    callableChain.call(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    });

    assertEquals("scout-client-thread-5 (Running)", threadName.getValue());
    assertEquals("scout-thread-5 (Idle)", Thread.currentThread().getName());

    ThreadInfo.CURRENT.remove();
    IFuture.CURRENT.remove();
  }

  @Test
  @Times(50)
  // This test is executed 50 times (regression)
  public void testThreadNameBlocking() throws Exception {
    final IMutex mutex = Jobs.newMutex();
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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1")
        .withMutex(mutex));

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
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("job-1")
        .withMutex(mutex));

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

  @SuppressWarnings("unchecked")
  private IFuture<?> mockFuture() {
    IFuture futureMock = mock(IFuture.class);
    when(futureMock.addListener(any(IFilter.class), any(IJobListener.class))).thenReturn(mock(IJobListenerRegistration.class));
    return futureMock;
  }
}
