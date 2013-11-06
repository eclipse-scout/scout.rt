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

import java.util.Calendar;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.testenvironment.TestEnvironmentServerSession;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.junit.Assert;
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
    Assert.assertEquals("JobCount must be 2", 2, scheduler.getJobCount());
    scheduler.start();

    Thread.sleep(1500); //now, JobAcceptTick should be running
    Assert.assertEquals("JobAcceptTick should be running only", 1, scheduler.getRunningJobCount());
    Assert.assertEquals("JobAcceptTick should be running only", 1, scheduler.getRunningJobs(null, null).size());
    Assert.assertEquals("2 Jobs should be in the Scheduler", 2, scheduler.getAllJobs().size());
    Assert.assertEquals("2 Jobs should be in the Scheduler", 2, scheduler.getJobCount());

    Thread.sleep(1000); //now JobAcceptTick should be finished
    Assert.assertEquals("No running job left", 0, scheduler.getRunningJobCount());
    Assert.assertEquals("No running job left", 0, scheduler.getRunningJobs(null, null).size());
    Assert.assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getAllJobs().size());
    Assert.assertEquals("jobDontAccept should be in the Scheduler", 1, scheduler.getJobCount());
    scheduler.stop();
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
