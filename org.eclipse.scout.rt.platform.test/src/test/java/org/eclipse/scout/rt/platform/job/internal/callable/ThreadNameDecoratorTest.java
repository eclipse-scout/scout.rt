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

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BeanData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  private JobManager m_jobManager;
  private IBean<JobManager> m_bean;

  @Before
  public void before() {
    m_jobManager = new JobManager();

    BeanData b = new BeanData<>(JobManager.class, m_jobManager);
    b.addAnnotation(AnnotationFactory.createApplicationScoped());
    b.addAnnotation(AnnotationFactory.createOrder(-10000));
    m_bean = Platform.get().getBeanContext().registerBean(b);
  }

  @After
  public void after() {
    m_jobManager.shutdown();
    Platform.get().getBeanContext().unregisterBean(m_bean);
  }

  @Test
  public void testThreadName() throws Exception {
    final StringHolder threadName = new StringHolder();

    ICallable<Void> next = new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    JobInput input = Jobs.newInput(RunContexts.empty()).id("123").name("job1");

    ThreadInfo.CURRENT.set(new ThreadInfo("scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.identifier()).call();
    assertEquals("scout-client-thread-5 [Running] 123:job1", threadName.getValue());
    assertEquals("scout-thread-5 [Idle]", Thread.currentThread().getName());
    ThreadInfo.CURRENT.remove();
  }

  @Test
  public void testThreadNameWithEmptyJobIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    ICallable<Void> next = new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    JobInput input = Jobs.newInput(RunContexts.empty());

    ThreadInfo.CURRENT.set(new ThreadInfo("scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.identifier()).call();
    assertEquals("scout-client-thread-5 [Running]", threadName.getValue());
    assertEquals("scout-thread-5 [Idle]", Thread.currentThread().getName());
    ThreadInfo.CURRENT.remove();
  }

  @Test
  public void testThreadNameBlocking() throws Exception {
    final Object mutexObject = new Object();
    final IBlockingCondition BC = m_jobManager.createBlockingCondition("blocking-condition", true);
    final Holder<Thread> threadJob1 = new Holder<>();

    // Job-1 (same mutex as job-2)
    IFuture<Boolean> future1 = m_jobManager.schedule(new ICallable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        threadJob1.setValue(Thread.currentThread());

        String currentThreadName = Thread.currentThread().getName();
        assertTrue(currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\[Running\\] job-1"));

        // Start blocking
        BC.waitFor();

        currentThreadName = Thread.currentThread().getName();
        assertTrue(currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\[Running\\] job-1"));
        return true;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()).name("job-1").mutex(mutexObject));

    // Job-2 (same mutex as job-1)
    IFuture<Boolean> future2 = m_jobManager.schedule(new ICallable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        String threadNameJob1 = threadJob1.getValue().getName();

        assertTrue(threadNameJob1, threadJob1.getValue().getName().matches("scout-thread-\\d+ \\[Blocked 'blocking-condition'\\] job-1"));

        // Release job-1
        BC.setBlocking(false);

        return true;
      }
    }, Jobs.newInput(RunContexts.copyCurrent()).name("job-1").mutex(mutexObject));

    assertTrue(future2.awaitDoneAndGet());
    assertTrue(future1.awaitDoneAndGet());
  }
}
