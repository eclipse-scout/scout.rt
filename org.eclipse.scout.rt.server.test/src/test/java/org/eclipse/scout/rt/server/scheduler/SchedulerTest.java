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
package org.eclipse.scout.rt.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil.ICondition;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for IScheduler
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class SchedulerTest {

  private ServerRunContext m_runContext;
  private Ticker m_ticker;

  @Before
  public void before() {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("john"));
    subject.setReadOnly();

    m_runContext = ServerRunContexts.empty();
    m_runContext.withSubject(subject);
    m_runContext.withSession(BEANS.get(ServerSessionProvider.class).provide(m_runContext.copy()));

    m_ticker = new Ticker(Calendar.SECOND);
  }

  @Test
  public void testRunningJobCount() throws InterruptedException {
    final IScheduler scheduler = new Scheduler(m_ticker, m_runContext);
    final BlockingCountDownLatch job1RunningLatch = new BlockingCountDownLatch(1);

    // Add 'job-1'(accepts the first tick)
    scheduler.addJob(new AbstractSchedulerJob("groupId", "job-1") {

      @Override
      public void run(IScheduler s, TickSignal signal) {
        try {
          job1RunningLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          // NOOP
        }

        setDisposed(true); // Unregister the job
      }

      @Override
      public boolean acceptTick(TickSignal signal) {
        return true;
      }
    });

    // Add 'job-2'(does not accept ticks)
    scheduler.addJob(new AbstractSchedulerJob("groupId", "job2") {

      @Override
      public boolean acceptTick(TickSignal signal) {
        return false;
      }
    });

    assertEquals("job count expected to be 2", 2, scheduler.getJobCount());
    scheduler.start();

    // Wait until job-1 is running
    assertTrue(job1RunningLatch.await());
    assertEquals("job-1 should be running only", 1, scheduler.getRunningJobCount());
    assertEquals("job-1 should be running only", 1, scheduler.getRunningJobs(null, null).size());
    assertEquals("2 jobs should be registered", 2, scheduler.getAllJobs().size());
    assertEquals("2 jobs should be registered", 2, scheduler.getJobCount());

    // Let job-1 finish
    job1RunningLatch.unblock();

    // Wait until job-1 completed
    JobTestUtil.waitForCondition(new ICondition() {

      @Override
      public boolean isFulfilled() {
        return scheduler.getJobCount() == 1;
      }
    });

    assertEquals("No running job left", 0, scheduler.getRunningJobCount());
    assertEquals("No running job left", 0, scheduler.getRunningJobs(null, null).size());
    assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getAllJobs().size());
    assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getJobCount());
  }
}
