/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.testenvironment.TestEnvironmentServerSession;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for IScheduler
 */
@RunWith(ScoutServerTestRunner.class)
public class SchedulerTest {
  @Test
  public void testRunningJobCount() throws ProcessingException, InterruptedException {
    IScheduler scheduler = new Scheduler(new Subject(), TestEnvironmentServerSession.class, new Ticker(Calendar.SECOND));
    scheduler.addJob(new JobAcceptTick("groupId", "jobIdAccept"));
    scheduler.addJob(new JobDontAcceptTick("groupId", "jobIdDontAccept"));
    assertEquals("JobCount must be 2", 2, scheduler.getJobCount());
    scheduler.start();

    Thread.sleep(1500); //now, JobAcceptTick should be running
    assertEquals("JobAcceptTick should be running only", 1, scheduler.getRunningJobCount());
    assertEquals("JobAcceptTick should be running only", 1, scheduler.getRunningJobs(null, null).size());
    assertEquals("2 Jobs should be in the Scheduler", 2, scheduler.getAllJobs().size());
    assertEquals("2 Jobs should be in the Scheduler", 2, scheduler.getJobCount());

    Thread.sleep(1000); //now JobAcceptTick should be finished
    assertEquals("No running job left", 0, scheduler.getRunningJobCount());
    assertEquals("No running job left", 0, scheduler.getRunningJobs(null, null).size());
    assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getAllJobs().size());
    assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getJobCount());
  }

  @Test
  public void testServerJobName() throws Throwable {
    IScheduler scheduler = new Scheduler(new Subject(), TestEnvironmentServerSession.class, new Ticker(Calendar.SECOND));
    scheduler.start();
    JobAcceptTick job = new JobAcceptTick("groupId", "jobIdAccept");
    TickSignal tick = scheduler.getTicker().waitForNextTick();
    JobFinderThread jobFinderThread = new JobFinderThread();
    jobFinderThread.start();
    scheduler.handleJobExecution(job, tick);
    scheduler.stop();
    jobFinderThread.join();
    assertTrue("The 'Scheduler.groupId.jobIdAccept' Job wasn't found", jobFinderThread.foundJob());
  }

}

class JobAcceptTick extends AbstractSchedulerJob {

  public JobAcceptTick(String groupId, String jobId) {
    super(groupId, jobId);
  }

  @Override
  public void run(IScheduler scheduler, TickSignal signal) throws ProcessingException {
    try {
      //wait 2 seconds
      Thread.sleep(2000);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    setDisposed(true);
  }

  @Override
  public boolean acceptTick(TickSignal signal) {
    return true;
  }
}

class JobDontAcceptTick extends AbstractSchedulerJob {
  public JobDontAcceptTick(String groupId, String jobId) {
    super(groupId, jobId);
  }

  @Override
  public boolean acceptTick(TickSignal signal) {
    return false;
  }
}

class JobFinderThread extends Thread {
  private boolean m_foundJob = false;

  boolean foundJob() {
    return m_foundJob;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(500); //wait a little bit until JobAcceptTick is running
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    Job[] allJobs = Job.getJobManager().find(null);

    for (Job job : allJobs) {
      if (job.getName().equals("Scheduler.groupId.jobIdAccept")) {
        m_foundJob = true;
      }
    }
  }
}
