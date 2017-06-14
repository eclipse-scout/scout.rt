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
package org.eclipse.scout.rt.platform.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IFunction;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class TransactionProcessorTest {

  private List<IBean<?>> m_beans = new ArrayList<>();

  private ITransaction m_transaction;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    m_transaction = Mockito.spy(BEANS.get(ITransaction.class));

    m_beans.add(BEANS.getBeanManager().registerBean(
        new BeanMetaData(ITransaction.class).withOrder(-1000).withProducer(new IBeanInstanceProducer<ITransaction>() {
          @Override
          public ITransaction produce(IBean<ITransaction> bean) {
            return m_transaction;
          }
        })));
  }

  @After
  public void after() {
    for (IBean<?> bean : m_beans) {
      BEANS.getBeanManager().unregisterBean(bean);
    }
    m_beans.clear();
  }

  @Test
  public void testMandatoryWithoutExistingTransaction() throws Exception {
    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.MANDATORY));

    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          return "result";
        }
      });
      fail();
    }
    catch (TransactionRequiredException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testMandatoryWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.MANDATORY));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    assertEquals("result", result);
    assertSame(callingTransaction, actualTransaction.getValue());
    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testMandatoryWithExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");
    ITransaction callingTransaction = mock(ITransaction.class);

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.MANDATORY));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);

      assertSame(callingTransaction, actualTransaction.getValue());
      verifyZeroInteractions(m_transaction);
      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, never()).release();
      verify(callingTransaction, times(1)).addFailure(any(Exception.class));
    }
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndSuccess() throws Exception {
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertSame(m_transaction, actualTransaction.getValue());
    assertEquals("result", result);

    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndErrorOnCommit() throws Exception {
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    m_transaction.registerMember(new TestTransactionMember(exception));

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          return "result";
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);
      inOrder.verify(m_transaction, times(1)).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertSame(m_transaction, actualTransaction.getValue());
    assertEquals("result", result);

    verifyZeroInteractions(callingTransaction);
    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verifyZeroInteractions(callingTransaction);
      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndSuccess() throws Exception {
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRED));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertEquals("result", result);
    assertSame(m_transaction, actualTransaction.getValue());

    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndErrorOnCommit() throws Exception {
    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    ITransactionMember m = new TestTransactionMember(exception);

    m_transaction.registerMember(m);

    final Holder<ITransaction> actualTransaction = new Holder<>();
    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          return "result";
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, times(1)).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
    finally {
      m_transaction.unregisterMember(m);
    }
  }

  @Test
  public void testRequiredWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.REQUIRED));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertEquals("result", result);
    assertSame(callingTransaction, actualTransaction.getValue());

    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testRequiredWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);

    final RuntimeException exception = new RuntimeException("Expected JUnit exception");

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(callingTransaction)
        .withTransactionScope(TransactionScope.REQUIRED));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(callingTransaction, actualTransaction.getValue());

      verifyZeroInteractions(m_transaction);

      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, times(1)).addFailure(any(Throwable.class));
    }
  }

  @Test
  public void testTransactionMemberSuccess() throws Exception {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("abc");
    when(txMember.needsCommit()).thenReturn(true);
    when(txMember.commitPhase1()).thenReturn(true);

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withTransactionMembers(Arrays.asList(txMember)));

    chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        assertSame(txMember, ITransaction.CURRENT.get().getMember("abc"));
        return null;
      }
    });

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, times(1)).commitPhase1();
    inOrder.verify(txMember, times(1)).commitPhase2();
    inOrder.verify(txMember, never()).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @Test
  public void testTransactionMemberFailed() throws Exception {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("abc");
    when(txMember.needsCommit()).thenReturn(true);
    when(txMember.commitPhase1()).thenReturn(true);

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withTransactionMembers(Arrays.asList(txMember)));

    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          assertSame(txMember, ITransaction.CURRENT.get().getMember("abc"));
          throw new RuntimeException("JUnit exception");
        }
      });
      fail("exception expected");
    }
    catch (RuntimeException e) {
      // NOOP
    }

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, never()).commitPhase1();
    inOrder.verify(txMember, never()).commitPhase2();
    inOrder.verify(txMember, times(1)).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @SuppressWarnings("unchecked")
  @Test(expected = AssertionException.class)
  public void testTransactionMember_REQUIRED_TxPresent() throws Exception {
    final ITransactionMember txMember = mock(ITransactionMember.class);

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(mock(ITransaction.class))
        .withTransactionScope(TransactionScope.REQUIRED)
        .withTransactionMembers(Arrays.asList(txMember)));

    chain.call(mock(Callable.class));
  }

  @Test
  public void testTransactionMember_REQUIRED_TxNotPresent() throws Exception {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("abc");
    when(txMember.needsCommit()).thenReturn(true);
    when(txMember.commitPhase1()).thenReturn(true);

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(null)
        .withTransactionScope(TransactionScope.REQUIRED)
        .withTransactionMembers(Arrays.asList(txMember)));

    chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        assertSame(txMember, ITransaction.CURRENT.get().getMember("abc"));
        return null;
      }
    });

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, times(1)).commitPhase1();
    inOrder.verify(txMember, times(1)).commitPhase2();
    inOrder.verify(txMember, never()).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @SuppressWarnings("unchecked")
  @Test(expected = AssertionException.class)
  public void testTransactionMember_MANDATORY() throws Exception {
    final ITransactionMember txMember = mock(ITransactionMember.class);

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor<Object>()
        .withCallerTransaction(mock(ITransaction.class))
        .withTransactionScope(TransactionScope.MANDATORY)
        .withTransactionMembers(Arrays.asList(txMember)));

    chain.call(mock(Callable.class));
  }

  @Test
  public void testRegisterMemberIfAbsentAndNotCancelled() {
    BasicTransaction t = new BasicTransaction();
    assertNotNull("expected non null transaction member object",
        t.registerMemberIfAbsentAndNotCancelled("123", new IFunction<String, TestTransactionMember>() {

          @Override
          public TestTransactionMember apply(String memberId) {
            return new TestTransactionMember(null);
          }

        }));
  }

  @Test
  public void testRegisterMemberIfAbsentAndNotCancelledOnCancelledTransaction() {
    BasicTransaction t = new BasicTransaction();
    t.cancel(false);
    assertNull("expected null transaction member object",
        t.registerMemberIfAbsentAndNotCancelled("123", new IFunction<String, TestTransactionMember>() {

          @Override
          public TestTransactionMember apply(String memberId) {
            fail("apply function must not be invoked; transaction was cancelled");
            return null;
          }

        }));
  }

  @Test
  public void testRegisterMemberIfAbsent() {
    BasicTransaction t = new BasicTransaction();
    assertNotNull("expected non null transaction member object",
        t.registerMemberIfAbsent("123", new IFunction<String, TestTransactionMember>() {

          @Override
          public TestTransactionMember apply(String memberId) {
            return new TestTransactionMember(null);
          }

        }));
  }

  @Test(expected = FutureCancelledError.class)
  public void testRegisterMemberIfAbsentOnCancelledTransaction() {
    BasicTransaction t = new BasicTransaction();
    t.cancel(false);
    t.registerMemberIfAbsent("123", new IFunction<String, TestTransactionMember>() {

      @Override
      public TestTransactionMember apply(String memberId) {
        return new TestTransactionMember(null);
      }

    });
  }

  public static class TestTransactionMember implements ITransactionMember {

    private final RuntimeException m_exception;

    TestTransactionMember(RuntimeException e) {
      m_exception = e;
    }

    @Override
    public void rollback() {
    }

    @Override
    public void release() {
    }

    @Override
    public boolean needsCommit() {
      return true;
    }

    @Override
    public String getMemberId() {
      return getClass().getSimpleName();
    }

    @Override
    public void commitPhase2() {
    }

    @Override
    public boolean commitPhase1() {
      throw m_exception;
    }

    @Override
    public void cancel() {
    }
  }
}
