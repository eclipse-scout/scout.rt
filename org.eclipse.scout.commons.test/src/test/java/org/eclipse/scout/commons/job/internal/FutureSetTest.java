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
import static org.mockito.Mockito.when;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.FutureSet.IFutureSupplier;
import org.eclipse.scout.commons.job.internal.Futures.JobFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FutureSetTest {

  @Mock
  private IFuture<Void> m_iFuture;

  @Mock
  private JobFuture<Void> m_jobFuture;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    when(m_jobFuture.getFuture()).thenReturn(m_iFuture);
  }

  @Test
  public void testPut() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    IFuture<Void> iFuture = futureSet.add(new IFutureSupplier<Void>() {

      @Override
      public JobFuture<Void> supply() {
        return m_jobFuture;
      }
    });

    assertSame(m_iFuture, iFuture);
    assertEquals(CollectionUtility.hashSet(m_iFuture), futureSet.values());
    assertFalse(m_iFuture.isCancelled());
    assertFalse(m_iFuture.isDone());
  }

  @Test
  public void testPutAndRemove() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add(new IFutureSupplier<Void>() {

      @Override
      public JobFuture<Void> supply() {
        return m_jobFuture;
      }
    });

    assertFalse(futureSet.isEmpty());
    futureSet.remove(m_iFuture);
    assertTrue(futureSet.isEmpty());
  }

  @Test
  public void testPutAndClear() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add(new IFutureSupplier<Void>() {

      @Override
      public JobFuture<Void> supply() {
        return m_jobFuture;
      }
    });

    assertFalse(futureSet.isEmpty());
    assertEquals(CollectionUtility.hashSet(m_iFuture), futureSet.clear());
    assertTrue(futureSet.isEmpty());
  }

  @Test(expected = AssertionException.class)
  public void testPutNullFuture() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    futureSet.add(new IFutureSupplier<Void>() {

      @Override
      public JobFuture<Void> supply() {
        return null;
      }
    });
  }

  @Test
  public void testPutRejected() throws JobExecutionException {
    FutureSet futureSet = new FutureSet();
    try {
      futureSet.add(new IFutureSupplier<Void>() {

        @Override
        public JobFuture<Void> supply() {
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
      assertFalse(m_iFuture.isCancelled());
      assertFalse(m_iFuture.isDone());
    }
  }
}
