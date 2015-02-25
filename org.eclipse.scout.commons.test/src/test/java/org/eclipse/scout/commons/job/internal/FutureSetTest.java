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
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.FutureSet.FutureSupplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FutureSetTest {

  @Mock
  private Future<Void> m_future;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPut() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    IFuture<Void> iFuture = futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return m_future;
      }
    });

    assertSame(m_future, iFuture.getDelegate());
    assertEquals(CollectionUtility.hashSet(m_future), futureSet.copy());
    assertFalse(m_future.isCancelled());
    assertFalse(m_future.isDone());
  }

  @Test
  public void testPutAndRemove() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return m_future;
      }
    });

    assertFalse(futureSet.isEmpty());
    futureSet.remove(m_future);
    assertTrue(futureSet.isEmpty());
  }

  @Test
  public void testPutAndClear() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return m_future;
      }
    });

    assertFalse(futureSet.isEmpty());
    assertEquals(CollectionUtility.hashSet(m_future), futureSet.clear());
    assertTrue(futureSet.isEmpty());
  }

  @Test(expected = AssertionException.class)
  public void testPutNullFuture() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return null;
      }
    });
  }

  @Test
  public void testPutRejected() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    try {
      futureSet.add("job", new FutureSupplier<Void>() {

        @Override
        public Future<Void> get() {
          throw new RejectedExecutionException();
        }
      });
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isRejection());
      assertFalse(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());

      assertTrue(futureSet.isEmpty());
      assertFalse(m_future.isCancelled());
      assertFalse(m_future.isDone());
    }
  }

  @Test
  public void testPutCancelled() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    IFuture<Void> iFuture = futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        when(m_future.isCancelled()).thenReturn(true);
        return m_future;
      }
    });

    assertTrue(futureSet.isEmpty());
    assertSame(m_future, iFuture.getDelegate());
    assertTrue(m_future.isCancelled());
    assertFalse(m_future.isDone());
  }

  @Test
  public void testPutDone() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    IFuture<Void> iFuture = futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        when(m_future.isDone()).thenReturn(true);
        return m_future;
      }
    });

    assertTrue(futureSet.isEmpty());
    assertSame(m_future, iFuture.getDelegate());
    assertTrue(m_future.isDone());
    assertFalse(m_future.isCancelled());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVisitAll() throws JobExecutionException {
    final Future<Void> future1 = mock(Future.class);
    final Future<Void> future2 = mock(Future.class);

    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future1;
      }
    });
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future2;
      }
    });

    assertEquals(CollectionUtility.hashSet(future1, future2), futureSet.copy());

    final Set<Future<?>> visitedFutures = new HashSet<>();
    futureSet.visit(new IFutureVisitor() {

      @Override
      public boolean visit(Future<?> future) {
        visitedFutures.add(future);
        return true;
      }
    });
    assertEquals(CollectionUtility.hashSet(future1, future2), futureSet.copy());
    assertEquals(CollectionUtility.hashSet(future1, future2), visitedFutures);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVisitAbort() throws JobExecutionException {
    final Future<Void> future1 = mock(Future.class);
    final Future<Void> future2 = mock(Future.class);

    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future1;
      }
    });
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future2;
      }
    });

    assertEquals(CollectionUtility.hashSet(future1, future2), futureSet.copy());

    final Set<Future<?>> visitedFutures = new HashSet<>();
    futureSet.visit(new IFutureVisitor() {

      @Override
      public boolean visit(Future<?> future) {
        visitedFutures.add(future);
        return false;
      }
    });
    assertEquals(CollectionUtility.hashSet(future1, future2), futureSet.copy());
    assertEquals(1, visitedFutures.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVisitDoneFuture() throws JobExecutionException {
    final Future<Void> future1 = mock(Future.class);
    final Future<Void> future2 = mock(Future.class);

    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future1;
      }
    });
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future2;
      }
    });

    // 'future1' is done in the meantime.
    when(future1.isDone()).thenReturn(true);

    final Set<Future<?>> visitedFutures = new HashSet<>();
    futureSet.visit(new IFutureVisitor() {

      @Override
      public boolean visit(Future<?> future) {
        visitedFutures.add(future);
        return true;
      }
    });
    assertEquals(CollectionUtility.hashSet(future2), visitedFutures);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVisitCancelledFuture() throws JobExecutionException {
    final Future<Void> future1 = mock(Future.class);
    final Future<Void> future2 = mock(Future.class);

    FutureSet futureSet = new FutureSet();
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future1;
      }
    });
    futureSet.add("job", new FutureSupplier<Void>() {

      @Override
      public Future<Void> get() {
        return future2;
      }
    });

    // 'future1' is cancelled in the meantime.
    when(future1.isCancelled()).thenReturn(true);

    final Set<Future<?>> visitedFutures = new HashSet<>();
    futureSet.visit(new IFutureVisitor() {

      @Override
      public boolean visit(Future<?> future) {
        visitedFutures.add(future);
        return true;
      }
    });
    assertEquals(CollectionUtility.hashSet(future1, future2), visitedFutures);
  }
}
