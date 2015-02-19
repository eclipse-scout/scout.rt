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
package org.eclipse.scout.commons.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.JobMap.IPutCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class JobMapTest {

  private IJob<Void> m_job1;
  private IJob<Void> m_job2;
  private IJob<Void> m_job3;
  private IJob<Void> m_job4;

  @Mock
  private IPutCallback<Void, Future<Void>> m_putCallbackMock1;
  @Mock
  private IPutCallback<Void, Future<Void>> m_putCallbackMock2;
  @Mock
  private IPutCallback<Void, Future<Void>> m_putCallbackMock3;
  @Mock
  private IPutCallback<Void, Future<Void>> m_putCallbackMock4;

  private Future<Void> m_future1;
  private Future<Void> m_future2;
  private Future<Void> m_future3;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_job1 = new Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        return null;
      }
    };
    m_job2 = new Job<Void>("job-2") {

      @Override
      protected Void call() throws Exception {
        return null;
      }
    };
    m_job3 = new Job<Void>("job-3") {

      @Override
      protected Void call() throws Exception {
        return null;
      }
    };
    m_job4 = new Job<Void>("job-4") {

      @Override
      protected Void call() throws Exception {
        return null;
      }
    };

    when(m_putCallbackMock1.onAbsent()).thenAnswer(new Answer<Future<Void>>() {

      @SuppressWarnings("unchecked")
      @Override
      public Future<Void> answer(InvocationOnMock invocation) throws Throwable {
        return m_future1 = new FutureTask<Void>(mock(Callable.class));
      }
    });
    when(m_putCallbackMock2.onAbsent()).thenAnswer(new Answer<Future<Void>>() {

      @SuppressWarnings("unchecked")
      @Override
      public Future<Void> answer(InvocationOnMock invocation) throws Throwable {
        return m_future2 = new FutureTask<Void>(mock(Callable.class));
      }
    });
    when(m_putCallbackMock3.onAbsent()).thenAnswer(new Answer<Future<Void>>() {

      @SuppressWarnings("unchecked")
      @Override
      public Future<Void> answer(InvocationOnMock invocation) throws Throwable {
        return m_future3 = new FutureTask<Void>(mock(Callable.class));
      }
    });
    when(m_putCallbackMock4.onAbsent()).thenAnswer(new Answer<Future<Void>>() {

      @SuppressWarnings("unchecked")
      @Override
      public Future<Void> answer(InvocationOnMock invocation) throws Throwable {
        return new FutureTask<Void>(mock(Callable.class));
      }
    });
  }

  @Test
  public void testPut() throws Exception {
    JobMap map = new JobMap();

    // 1. Put the Job into the map
    //    Expected: job is put into the map
    Future<Void> future = map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
    assertEquals(CollectionUtility.hashSet(m_job1), map.copyJobMap().keySet());
    assertSame(m_future1, map.getFuture(m_job1));
    verify(m_putCallbackMock1, times(1)).onAbsent();

    // 2. Try to put the same job into the map again
    //    Expected: job is rejected with a RejectedExecutionException
    try {
      map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isRejection());
    }
    assertEquals(CollectionUtility.hashSet(m_job1), map.copyJobMap().keySet());
    assertSame(m_future1, map.getFuture(m_job1));
    verify(m_putCallbackMock1, times(1)).onAbsent(); // not called anymore

    // 3. Remove the job associated with the given future.
    map.remove(future);
    assertTrue(map.copyJobMap().isEmpty());

    // 4. Put the Job into the map anew (reschedule)
    //    Expected: job is put into the map
    map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
    assertEquals(CollectionUtility.hashSet(m_job1), map.copyJobMap().keySet());
    assertSame(m_future1, map.getFuture(m_job1));
    verify(m_putCallbackMock1, times(2)).onAbsent();
  }

  @Test
  public void testPutCanceledJob() throws Exception {
    JobMap map = new JobMap();

    map.putIfAbsentElseReject(m_job1, new IPutCallback<Void, Future<Void>>() {

      @Override
      public Future<Void> onAbsent() {
        @SuppressWarnings("unchecked")
        Future<Void> future = mock(Future.class);
        when(future.isCancelled()).thenReturn(true);
        when(future.isDone()).thenReturn(false);
        return future;
      }
    });
    assertTrue(map.isEmpty());
  }

  @Test
  public void testPutDoneJob() throws Exception {
    JobMap map = new JobMap();

    map.putIfAbsentElseReject(m_job1, new IPutCallback<Void, Future<Void>>() {

      @Override
      public Future<Void> onAbsent() {
        @SuppressWarnings("unchecked")
        Future<Void> future = mock(Future.class);
        when(future.isCancelled()).thenReturn(false);
        when(future.isDone()).thenReturn(true);
        return future;
      }
    });
    assertTrue(map.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVisit() throws Exception {
    Future<Void> future1 = mock(Future.class);
    when(m_putCallbackMock1.onAbsent()).thenReturn(future1);

    Future<Void> future2 = mock(Future.class);
    when(m_putCallbackMock2.onAbsent()).thenReturn(future2);

    JobMap map = new JobMap();
    map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
    map.putIfAbsentElseReject(m_job2, m_putCallbackMock2);
    map.putIfAbsentElseReject(m_job3, m_putCallbackMock3);
    map.putIfAbsentElseReject(m_job4, m_putCallbackMock4);
    assertEquals(4, map.size());

    when(future1.isCancelled()).thenReturn(true);
    when(future2.isDone()).thenReturn(true);

    final Set<IJob<?>> actualVisitedJobs = new HashSet<>();
    map.visit(new IJobVisitor() {

      @Override
      public boolean visit(IJob<?> job) {
        actualVisitedJobs.add(job);
        return true;
      }
    });
    assertEquals(CollectionUtility.hashSet(m_job1, m_job3, m_job4), actualVisitedJobs);
  }

  @Test
  public void testGetFuture() throws Exception {
    JobMap map = new JobMap();

    map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
    map.putIfAbsentElseReject(m_job2, m_putCallbackMock2);
    map.putIfAbsentElseReject(m_job3, m_putCallbackMock3);

    assertSame(m_future1, map.getFuture(m_job1));
    assertSame(m_future2, map.getFuture(m_job2));
    assertSame(m_future3, map.getFuture(m_job3));
  }

  @Test
  public void testCancel() throws Exception {
    JobMap map = new JobMap();

    // verify querying cancel-status of not registered jobs.
    assertFalse(map.isCancelled(m_job1));
    assertFalse(map.isCancelled(m_job2));

    assertFalse(map.cancel(m_job1, true));
    assertFalse(map.cancel(m_job2, false));

    assertFalse(map.isCancelled(m_job1));
    assertFalse(map.isCancelled(m_job2));

    assertTrue(map.isEmpty());

    // Put jobs into the map.
    map.putIfAbsentElseReject(m_job1, m_putCallbackMock1);
    map.putIfAbsentElseReject(m_job2, m_putCallbackMock2);

    assertEquals(2, map.size());
    assertTrue(map.cancel(m_job1, true));
    assertTrue(map.cancel(m_job2, false));

    assertTrue(map.isCancelled(m_job1)); // should be canceled
    assertTrue(map.isCancelled(m_job2)); // should be canceled
    assertFalse(map.cancel(m_job1, true)); // cancel again
    assertFalse(map.cancel(m_job2, false)); // cancel again
    assertTrue(map.isCancelled(m_job1)); // should still be canceled
    assertTrue(map.isCancelled(m_job2)); // should still be canceled

    // remove the jobs from the map
    assertTrue(map.remove(m_future1));
    assertEquals(1, map.size());
    assertFalse(map.remove(m_future1));

    assertTrue(map.remove(m_future2));
    assertEquals(0, map.copyJobMap().size());
    assertFalse(map.remove(m_future2));

    assertFalse(map.isCancelled(m_job1)); // should not be contained in the map
    assertFalse(map.isCancelled(m_job2)); // should not be contained in the map
  }
}
