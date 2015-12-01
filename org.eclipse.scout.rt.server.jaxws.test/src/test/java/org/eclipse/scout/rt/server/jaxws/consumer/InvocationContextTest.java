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
package org.eclipse.scout.rt.server.jaxws.consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.jws.WebMethod;
import javax.xml.ws.BindingProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.jaxws.JaxWsTestServerSession;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(JaxWsTestServerSession.class)
@RunWithSubject("jaxws-user")
public class InvocationContextTest {

  private TestPort m_port;
  private ICommitListener m_commitListener;
  private IRollbackListener m_rollbackListener;

  private JaxWsImplementorSpecifics m_implementorSpecifics;

  private List<IBean<?>> m_beans;

  @Before
  public void before() {
    m_port = mock(TestPort.class);
    when(m_port.getRequestContext()).thenReturn(new HashMap<String, Object>());
    m_commitListener = mock(ICommitListener.class);
    m_rollbackListener = mock(IRollbackListener.class);
    m_implementorSpecifics = mock(JaxWsImplementorSpecifics.class);

    when(m_commitListener.onCommitPhase1()).thenReturn(true);
    m_beans = TestingUtility.registerBeans(new BeanMetaData(BEANS.get(JaxWsImplementorSpecifics.class).getClass(), m_implementorSpecifics).withReplace(true));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Test
  public void testWithSuccess() {
    final Holder<ITransaction> currentTransaction = new Holder<>();
    final Holder<ITransaction> invocationTransaction = new Holder<>();
    final Holder<IServerSession> invocationServerSession = new Holder<>();

    ServerRunContexts.copyCurrent().withTransactionScope(TransactionScope.REQUIRES_NEW).run(new IRunnable() { // set transaction boundary

      @Override
      public void run() throws Exception {
        currentTransaction.setValue(ITransaction.CURRENT.get());

        InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
        invocationContext.withEndpointUrl("http://localhost");
        invocationContext.whenCommit(m_commitListener);
        invocationContext.whenRollback(m_rollbackListener);
        invocationContext.whenInvoke(new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            invocationTransaction.setValue(ITransaction.CURRENT.get());
            invocationServerSession.setValue(ServerSessionProvider.currentSession());

            return method.invoke(proxy, args);
          }
        });

        // run the test
        invocationContext.getPort().webMethod();
      }
    });

    assertSame(currentTransaction.getValue(), invocationTransaction.getValue());
    assertSame(ISession.CURRENT.get(), invocationServerSession.getValue());

    verify(m_port).webMethod();
    verify(m_commitListener).onCommitPhase1();
    verify(m_commitListener).onCommitPhase2();
    verify(m_rollbackListener, never()).onRollback();
  }

  @Test
  public void testWithException() {
    final Holder<ITransaction> currentTransaction = new Holder<>();
    final Holder<ITransaction> invocationTransaction = new Holder<>();
    final Holder<IServerSession> invocationServerSession = new Holder<>();
    final Holder<Exception> invocationException = new Holder<>();

    // simulate that 'webMethod' throws an exception.
    final RuntimeException exception = new RuntimeException();
    doThrow(exception).when(m_port).webMethod();

    try {
      ServerRunContexts.copyCurrent().withTransactionScope(TransactionScope.REQUIRES_NEW).run(new IRunnable() { // set transaction boundary

        @Override
        public void run() throws Exception {
          currentTransaction.setValue(ITransaction.CURRENT.get());

          InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
          invocationContext.withEndpointUrl("http://localhost");

          invocationContext.whenCommit(m_commitListener);
          invocationContext.whenRollback(m_rollbackListener);
          invocationContext.whenInvoke(new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              invocationTransaction.setValue(ITransaction.CURRENT.get());
              invocationServerSession.setValue(ServerSessionProvider.currentSession());

              return method.invoke(proxy, args);
            }
          });

          // run the test
          try {
            invocationContext.getPort().webMethod();
          }
          catch (Exception e) {
            invocationException.setValue(e);
            throw e;
          }
        }
      });
    }
    catch (ProcessingException e) {
      // NOOP
    }

    assertSame(currentTransaction.getValue(), invocationTransaction.getValue());
    assertSame(ISession.CURRENT.get(), invocationServerSession.getValue());

    verify(m_port).webMethod();
    verify(m_commitListener, never()).onCommitPhase1();
    verify(m_commitListener, never()).onCommitPhase2();
    verify(m_rollbackListener).onRollback();
    assertSame(invocationException.getValue(), exception);
  }

  @Test
  public void testInvokeNotWebMethod() {
    final BooleanHolder intercepted = new BooleanHolder(false);

    InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
    invocationContext.withEndpointUrl("http://localhost");
    invocationContext.whenInvoke(new InvocationHandler() {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        intercepted.setValue(true);
        return method.invoke(proxy, args);
      }
    });

    invocationContext.getPort().notWebMethod(); // invoke method which is not annotated with @WebMethod
    assertFalse(intercepted.getValue()); // only methods annotated with @WebMethod are intercepted
    verify(m_port).notWebMethod();
  }

  @Test
  public void testCancel() {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);

    final InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
    invocationContext.withEndpointUrl("http://localhost");

    // Make Stub.webMethod to block until cancelled.
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          e.printStackTrace();
          // NOOP
        }

        return null;
      }
    }).when(m_port).webMethod();

    final RunMonitor runMonitor = new RunMonitor();

    // Cancel the 'webMethod' once blocking.
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        setupLatch.await();
        runMonitor.cancel(true);
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test by invoking the web service with a specific RunMonitor to test cancellation.
    RunContexts.empty().withRunMonitor(runMonitor).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          invocationContext.getPort().webMethod(); // this method blocks until cancelled.
          fail();
        }
        catch (CancellationException e) {
          verify(m_implementorSpecifics).closeSocket(same(m_port), anyString());
        }
      }
    });
  }

  interface TestPort extends BindingProvider {

    @WebMethod
    void webMethod();

    void notWebMethod();
  }
}
