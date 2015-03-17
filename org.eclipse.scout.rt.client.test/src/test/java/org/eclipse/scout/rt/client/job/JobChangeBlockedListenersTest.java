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
package org.eclipse.scout.rt.client.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobChangeListener;
import org.eclipse.scout.commons.job.IJobChangeListeners;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.job.internal.JobChangeEvent;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class JobChangeBlockedListenersTest {

  @Test
  public void testBlocking() throws Exception {
    final IModelJobManager jobMgr = OBJ.get(IModelJobManager.class);
    final IBlockingCondition condition = jobMgr.createBlockingCondition("test condition", true);
    P_JobChangeListener listener = new P_JobChangeListener();

    IJobChangeListeners.DEFAULT.add(listener, ModelJobFilter.INSTANCE);
    IFuture<Void> future = null;
    final IHolder<IFuture<?>> innerFuture = new Holder<IFuture<?>>();
    try {
      IClientSession clientSession = Mockito.mock(IClientSession.class);
      final ClientJobInput input = ClientJobInput.empty().session(clientSession);

      // start recording of events
      future = jobMgr.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          innerFuture.setValue(jobMgr.schedule(new IRunnable() {
            @Override
            public void run() throws Exception {
              condition.setBlocking(false);
            }
          }, 200, TimeUnit.MILLISECONDS, input));

          condition.waitFor();
        }
      }, input);
      jobMgr.waitUntilDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
      jobMgr.shutdown();
    }
    finally {
      IJobChangeListeners.DEFAULT.remove(listener, ModelJobFilter.INSTANCE);
    }

    // compare events with expected events
    List<Integer> expectedStati = CollectionUtility.arrayList(JobChangeEvent.EVENT_TYPE_SCHEDULED, JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_SCHEDULED, JobChangeEvent.EVENT_TYPE_BLOCKED,
        JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_DONE, JobChangeEvent.EVENT_TYPE_UN_BLOCKED, JobChangeEvent.EVENT_TYPE_DONE, JobChangeEvent.EVENT_TYPE_SHUTDOWN);
    List<IFuture<?>> expectedFutures = CollectionUtility.arrayList(future, future, innerFuture.getValue(), future, innerFuture.getValue(), innerFuture.getValue(), future, future, null);

    Assert.assertEquals(expectedStati.size(), listener.m_events.size());
    for (IJobChangeEvent evt : listener.m_events) {
      if (evt.getType() != JobChangeEvent.EVENT_TYPE_SHUTDOWN) {
        Assert.assertEquals(JobChangeEvent.EVENT_MODE_ASYNC, evt.getMode());
      }
      else {
        // shutdown event is always sync
        Assert.assertEquals(JobChangeEvent.EVENT_MODE_SYNC, evt.getMode());
      }
      Assert.assertEquals(expectedStati.remove(0).intValue(), evt.getType());
      Assert.assertSame(expectedFutures.remove(0), evt.getFuture());
    }
  }

  @Test
  public void testEventsAsync() throws Exception {
    doTest();
  }

  private void doTest() throws Exception {
    ModelJobManager jobMgr = new ModelJobManager();
    P_JobChangeListener listener = new P_JobChangeListener();
    IJobChangeListeners.DEFAULT.add(listener);
    IClientSession clientSession = Mockito.mock(IClientSession.class);

    ClientJobInput input = ClientJobInput.empty().session(clientSession);
    IFuture<Void> future = jobMgr.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
      }
    }, input);
    jobMgr.waitUntilDone(new FutureFilter(future), 1, TimeUnit.MINUTES);
    jobMgr.shutdown();
    IJobChangeListeners.DEFAULT.remove(listener);

    List<Integer> expectedStati = CollectionUtility.arrayList(JobChangeEvent.EVENT_TYPE_SCHEDULED, JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN, JobChangeEvent.EVENT_TYPE_DONE, JobChangeEvent.EVENT_TYPE_SHUTDOWN);
    Assert.assertEquals(expectedStati.size(), listener.m_events.size());
    for (IJobChangeEvent evt : listener.m_events) {
      if (evt.getType() != JobChangeEvent.EVENT_TYPE_SHUTDOWN) {
        Assert.assertSame(evt.getFuture(), future);
        Assert.assertSame(evt.getFuture().getJobInput(), input);
        Assert.assertEquals(JobChangeEvent.EVENT_MODE_ASYNC, evt.getMode());
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
