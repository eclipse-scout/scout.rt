/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.runner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 6.0
 */
@RunWithSubject("anna")
@RunWith(PlatformTestRunner.class)
@Ignore("this is only a test fixture for the PlatformTestRunnerTransactionTest")
public class PlatformTestRunnerTransactionTestFixture {

  private ITransactionMember m_txnMember;

  public void verifyTransaction(boolean expectingCommit) {
    if (expectingCommit) {
      verify(m_txnMember).commitPhase1();
      verify(m_txnMember).commitPhase2();
    }
    else {
      verify(m_txnMember).rollback();
    }
    verify(m_txnMember).release();
    verify(m_txnMember, atLeastOnce()).needsCommit();
    verify(m_txnMember, atLeastOnce()).getMemberId();
    verifyNoMoreInteractions(m_txnMember);
  }

  @Before
  public void before() {
    m_txnMember = mock(ITransactionMember.class);
    when(m_txnMember.getMemberId()).thenReturn("mock txn member");
    when(m_txnMember.needsCommit()).thenReturn(true);
    when(m_txnMember.commitPhase1()).thenReturn(true);
    ITransaction txn = ITransaction.CURRENT.get();
    assertNotNull(txn);
    txn.registerMember(m_txnMember);
  }

  @Test
  public void testOk() {
    assertTrue(true);
  }

  @Test
  public void testHandledException() {
    try {
      assertTrue(false);
      fail("should have failed");
    }
    catch (AssertionError expected) {
      // expected
    }
    assertTrue(true);
  }

  @Test(expected = PlatformException.class)
  public void testThrowingExpectedRuntimeException() {
    throw new PlatformException("expected exception");
  }

  @Test(expected = AssertionError.class)
  public void testThrowingExpectedAssertionError() {
    fail("expected fail");
  }

  @Test
  public void testThrowingUnexpectedRuntimeException() {
    throw new PlatformException("unexpected exception");
  }

  @Test
  public void testThrowingUnexpectedAssertionError() {
    fail("expected fail");
  }

  @Test(expected = RuntimeException.class)
  public void testExpectingRuntimeExceptionThatIsNotThrown() {
    assertTrue(true);
  }

  @Test(expected = AssertionError.class)
  public void testExpectingAssertionErrorThatIsNotThrown() {
    assertTrue(true);
  }

  @Test(expected = PlatformException.class)
  public void testExpectingPlatformExceptionButIllegalArgumentExceptionIsThrown() {
    throw new IllegalArgumentException("expected IAE");
  }

  @Test(expected = PlatformException.class)
  public void testExpectedExceptionHandledByExceptionHandler() {
    BEANS.get(ExceptionHandler.class).handle(new PlatformException("expected exception"));
  }

  @Test
  public void testUnexpectedExceptionHandledByExceptionHandler() {
    BEANS.get(ExceptionHandler.class).handle(new PlatformException("unexpected exception"));
  }
}
