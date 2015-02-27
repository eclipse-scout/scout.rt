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
package org.eclipse.scout.commons.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.job.internal.JobChangeEvent;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.junit.Assert;
import org.junit.Test;

public class JobChangeListenersTest {

  @Test
  public void testEventsSync() throws Exception {
    doTest(true);
  }

  @Test
  public void testEventsAsync() throws Exception {
    doTest(false);
  }

  @Test
  public void testCancel() throws Exception {
    JobManager<IJobInput> jobMgr = new JobManager<IJobInput>("job-change-test");
    P_JobChangeListener listener = new P_JobChangeListener();
    IJobChangeListeners.DEFAULT.add(listener);
    final BooleanHolder hasStarted = new BooleanHolder(Boolean.FALSE);
    IFuture<Void> future = jobMgr.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        hasStarted.setValue(Boolean.TRUE);
      }
    }, 200, TimeUnit.MILLISECONDS, JobInput.empty());
    future.cancel(true);
    jobMgr.waitUntilDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
    IJobChangeListeners.DEFAULT.remove(listener);
    jobMgr.shutdown();

    Assert.assertFalse(hasStarted.getValue().booleanValue());
    List<Integer> expectedStati = CollectionUtility.arrayList(JobChangeEvent.EVENT_TYPE_SCHEDULED, JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_DONE);
    Assert.assertEquals(expectedStati.size(), listener.m_events.size());
    for (IJobChangeEvent evt : listener.m_events) {
      Assert.assertEquals(expectedStati.remove(0).intValue(), evt.getType());
      Assert.assertTrue(evt.getFuture().isCancelled());
    }
  }

  private void doTest(boolean sync) throws Exception {
    JobManager<IJobInput> jobMgr = new JobManager<IJobInput>("job-change-test");
    P_JobChangeListener listener = new P_JobChangeListener();
    IJobChangeListeners.DEFAULT.add(listener);

    IFuture<Void> future = null;
    JobInput input = JobInput.empty();
    if (sync) {
      jobMgr.runNow(new IRunnable() {
        @Override
        public void run() throws Exception {
        }
      }, input);
    }
    else {
      future = jobMgr.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
        }
      }, input);
      jobMgr.waitUntilDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
    }
    jobMgr.shutdown();
    IJobChangeListeners.DEFAULT.remove(listener);

    List<Integer> expectedStati = null;
    int expectedMode = -1;
    if (sync) {
      expectedMode = JobChangeEvent.EVENT_MODE_SYNC;
      expectedStati = CollectionUtility.arrayList(JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_DONE, JobChangeEvent.EVENT_TYPE_SHUTDOWN);
    }
    else {
      expectedMode = JobChangeEvent.EVENT_MODE_ASYNC;
      expectedStati = CollectionUtility.arrayList(JobChangeEvent.EVENT_TYPE_SCHEDULED, JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_DONE, JobChangeEvent.EVENT_TYPE_SHUTDOWN);
    }

    Assert.assertEquals(expectedStati.size(), listener.m_events.size());
    for (IJobChangeEvent evt : listener.m_events) {
      if (evt.getType() != JobChangeEvent.EVENT_TYPE_SHUTDOWN) {
        if (!sync) {
          Assert.assertSame(evt.getFuture(), future);
          Assert.assertSame(evt.getFuture().getJobInput(), input);
        }
        Assert.assertEquals(expectedMode, evt.getMode());
      }
      else {
        // shutdown event has no future and is always sync
        Assert.assertEquals(JobChangeEvent.EVENT_MODE_SYNC, evt.getMode());
      }
      Assert.assertEquals(expectedStati.remove(0).intValue(), evt.getType());
    }
  }

  private static final class P_JobChangeListener implements IJobChangeListener {

    private final List<IJobChangeEvent> m_events = new ArrayList<>();

    @Override
    public void jobChanged(IJobChangeEvent event) {
      m_events.add(event);
    }
  }
}
