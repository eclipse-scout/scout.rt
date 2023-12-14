/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.jws.WebMethod;
import jakarta.xml.ws.BindingProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.jaxws.JaxWsTestServerSession;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

@RunWith(ServerTestRunner.class)
@RunWithServerSession(JaxWsTestServerSession.class)
@RunWithSubject("jaxws-user")
public class InvocationContextTest {

  private static final String TESTING_CORRELATION_ID = "testingCorrelationId";
  private TestPort m_port;
  private ICommitListener m_commitListener;
  private IRollbackListener m_rollbackListener;

  private JaxWsImplementorSpecifics m_implementorSpecifics;

  private List<IBean<?>> m_beans;

  @Before
  public void before() {
    m_port = mock(TestPort.class);
    when(m_port.getRequestContext()).thenReturn(new HashMap<>());
    m_commitListener = mock(ICommitListener.class);
    m_rollbackListener = mock(IRollbackListener.class);
    m_implementorSpecifics = mock(JaxWsImplementorSpecifics.class);

    when(m_commitListener.onCommitPhase1()).thenReturn(true);
    m_beans = BEANS.get(BeanTestingHelper.class).registerBeans(new BeanMetaData(BEANS.get(JaxWsImplementorSpecifics.class).getClass(), m_implementorSpecifics).withReplace(true));
  }

  @After
  public void after() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(m_beans);
  }

  @Test
  public void testWithSuccess() {
    final Holder<ITransaction> currentTransaction = new Holder<>();
    final Holder<ITransaction> invocationTransaction = new Holder<>();
    final Holder<IServerSession> invocationServerSession = new Holder<>();

    ServerRunContexts.copyCurrent()
        .withCorrelationId(TESTING_CORRELATION_ID)
        .withTransactionScope(TransactionScope.REQUIRES_NEW) // set transaction boundary
        .run(() -> {
          currentTransaction.setValue(ITransaction.CURRENT.get());

          InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
          invocationContext.withEndpointUrl("http://localhost");
          invocationContext.whenCommit(m_commitListener);
          invocationContext.whenRollback(m_rollbackListener);
          invocationContext.whenInvoke((proxy, method, args) -> {
            invocationTransaction.setValue(ITransaction.CURRENT.get());
            invocationServerSession.setValue(ServerSessionProvider.currentSession());

            return method.invoke(proxy, args);
          });

          // run the test
          invocationContext.getPort().webMethod();
        });

    assertSame(currentTransaction.getValue(), invocationTransaction.getValue());
    assertSame(ISession.CURRENT.get(), invocationServerSession.getValue());
    assertEquals(TESTING_CORRELATION_ID, m_port.getRequestContext().get(MessageContexts.PROP_CORRELATION_ID));

    verify(m_port).webMethod();
    verify(m_commitListener).onCommitPhase1();
    verify(m_commitListener).onCommitPhase2();
    verify(m_rollbackListener, never()).onRollback();
  }

  @Test
  public void testWithException() {
    // Unregister JUnit exception handler
    BEANS.getBeanManager().unregisterBean(BEANS.getBeanManager().getBean(JUnitExceptionHandler.class));

    final Holder<ITransaction> currentTransaction = new Holder<>();
    final Holder<ITransaction> invocationTransaction = new Holder<>();
    final Holder<IServerSession> invocationServerSession = new Holder<>();
    final Holder<Exception> callableException = new Holder<>();
    // simulate that 'webMethod' throws an exception.
    final RuntimeException exception = new RuntimeException();
    doThrow(exception).when(m_port).webMethod();

    try {
      ServerRunContexts.copyCurrent()
          .withCorrelationId(TESTING_CORRELATION_ID)
          .withTransactionScope(TransactionScope.REQUIRES_NEW) // set transaction boundary
          .run(() -> {
            currentTransaction.setValue(ITransaction.CURRENT.get());

            InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
            invocationContext.withEndpointUrl("http://localhost");

            invocationContext.whenCommit(m_commitListener);
            invocationContext.whenRollback(m_rollbackListener);
            invocationContext.whenInvoke((proxy, method, args) -> {
              invocationTransaction.setValue(ITransaction.CURRENT.get());
              invocationServerSession.setValue(ServerSessionProvider.currentSession());

              return method.invoke(proxy, args);
            });

            // run the test
            try {
              invocationContext.getPort().webMethod();
            }
            catch (Exception e) {
              callableException.setValue(e);
              throw e;
            }
          });

      fail("RuntimeException expected");
    }
    catch (RuntimeException e) {
      // NOOP
    }

    assertSame(currentTransaction.getValue(), invocationTransaction.getValue());
    assertSame(ISession.CURRENT.get(), invocationServerSession.getValue());
    assertEquals(TESTING_CORRELATION_ID, m_port.getRequestContext().get(MessageContexts.PROP_CORRELATION_ID));

    verify(m_port).webMethod();
    verify(m_commitListener, never()).onCommitPhase1();
    verify(m_commitListener, never()).onCommitPhase2();
    verify(m_rollbackListener).onRollback();
    assertSame(callableException.getValue(), exception);
  }

  @Test
  public void testInvokeNotWebMethod() {
    final BooleanHolder intercepted = new BooleanHolder(false);

    ServerRunContexts.copyCurrent()
        .withCorrelationId(TESTING_CORRELATION_ID)
        .run(() -> {
          InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
          invocationContext.withEndpointUrl("http://localhost");
          invocationContext.whenInvoke((proxy, method, args) -> {
            intercepted.setValue(true);
            return method.invoke(proxy, args);
          });

          invocationContext.getPort().notWebMethod(); // invoke method which is not annotated with @WebMethod
        });

    assertFalse(intercepted.getValue()); // only methods annotated with @WebMethod are intercepted
    assertNull(m_port.getRequestContext().get(MessageContexts.PROP_CORRELATION_ID)); // correlationId is not propagated other than web methods

    verify(m_port).notWebMethod();
  }

  @Test(timeout = 5000)
  public void testCancel() {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final IBlockingCondition processingCondition = Jobs.newBlockingCondition(true);

    final InvocationContext<TestPort> invocationContext = new InvocationContext<>(m_port, "name");
    invocationContext.withEndpointUrl("http://localhost");

    // Make Stub.webMethod to block until cancelled.
    doAnswer((Answer<Void>) invocation -> {
      setupLatch.countDown();
      processingCondition.waitFor(10, TimeUnit.SECONDS);
      return null;
    }).when(m_port).webMethod();

    final RunMonitor runMonitor = new RunMonitor();

    // Cancel the 'webMethod' once blocking.
    Jobs.schedule(() -> {
      setupLatch.await();
      runMonitor.cancel(true);
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    // Run the test by invoking the web service with a specific RunMonitor to test cancellation.
    try {
      RunContexts.empty().withRunMonitor(runMonitor).run(() -> {
        try {
          invocationContext.getPort().webMethod(); // this method blocks until cancelled.
          fail("WebServiceRequestCancelledException expected");
        }
        catch (WebServiceRequestCancelledException e) {
          verify(m_implementorSpecifics).closeSocket(same(m_port), anyString());
        }
      });
    }
    finally {
      processingCondition.setBlocking(false);
    }
  }

  interface TestPort extends BindingProvider {

    @WebMethod
    void webMethod();

    void notWebMethod();
  }
}
