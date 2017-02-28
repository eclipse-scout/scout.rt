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
package org.eclipse.scout.rt.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.fixture.MockServerProcessingCancelService;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests cancellation of client- and serverside processing (through proxy).
 */
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@RunWithSubject("anna")
@Times(20)
@Ignore
//TODO bsh, abr
public class ClientJobCancelTest {

  private List<IBean<?>> m_serviceReg;

  @Before
  public void before() throws Exception {

    m_serviceReg = TestingUtility.registerBeans(
        new BeanMetaData(MockServerProcessingCancelService.class)
            .withInitialInstance(new MockServerProcessingCancelService())
            .withApplicationScoped(true).withOrder(-1));

  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_serviceReg);
  }

  /**
   * Tests a request, which is not cancelled.
   */
  @Test
  public void testWithoutCancellation() throws Exception {
    assertEquals("ABC", doPingRequestAsync("abc").complete());
  }

  /**
   * Tests a request, which gets cancelled
   */
  @Test
  public void testCancellation() throws Exception {
    try {
      doPingRequestAsync("abc").cancel();
      fail("interruption expected");
    }
    catch (ThreadInterruptedError e) {
      // NOOP
    }
  }

  /**
   * Runs a 'ping-request' which gets blocked in the service implementation.
   */
  protected RequestData doPingRequestAsync(final String pingRequest) throws Exception {
    final BlockingCountDownLatch serviceCallSetupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch serviceCallCompletedLatch = new BlockingCountDownLatch(1);
    final AtomicBoolean serviceCallInterrupted = new AtomicBoolean(false);

    // Mock the PingService.
    class PingService implements IPingService {

      @Override
      public String ping(String s) {
        try {
          assertTrue(serviceCallSetupLatch.countDownAndBlock());
        }
        catch (java.lang.InterruptedException e) {
          serviceCallInterrupted.set(true);
        }
        finally {
          serviceCallCompletedLatch.countDown();
        }
        return s.toUpperCase();
      }
    }

    // Create a separate RunContext with a separate RunMonitor, so we can wait for the service result in case of cancellation.
    final ClientRunContext runContext = ClientRunContexts.copyCurrent();

    final RunMonitor runMonitor = BEANS.get(RunMonitor.class);
    runContext.withRunMonitor(runMonitor);

    IFuture<String> pingFuture = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        return runContext.call(new Callable<String>() {

          @Override
          public String call() throws Exception {
            IBean<?> bean = TestingUtility.registerBean(new BeanMetaData(PingService.class).withInitialInstance(new PingService()).withApplicationScoped(true));
            try {
              return ServiceTunnelUtility.createProxy(IPingService.class).ping(pingRequest);
            }
            finally {
              TestingUtility.unregisterBeans(Arrays.asList(bean));
            }
          }
        });
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));

    // Wait for the ping request to enter service implementation.
    assertTrue(serviceCallSetupLatch.await());

    return new RequestData(pingFuture, runMonitor, serviceCallSetupLatch, serviceCallCompletedLatch, serviceCallInterrupted);
  }

  private static class RequestData {

    private final IFuture<String> m_pingFuture;
    private final RunMonitor m_pingRunMonitor;
    private final BlockingCountDownLatch m_serviceCallLatch;
    private final BlockingCountDownLatch m_serviceCallCompletedLatch;
    private final AtomicBoolean m_serviceCallInterrupted;

    public RequestData(IFuture<String> pingFuture, RunMonitor pingRunMonitor, BlockingCountDownLatch serviceCallLatch, BlockingCountDownLatch serviceCallCompletedLatch, AtomicBoolean serviceCallInterrupted) {
      m_pingFuture = pingFuture;
      m_pingRunMonitor = pingRunMonitor;
      m_serviceCallLatch = serviceCallLatch;
      m_serviceCallCompletedLatch = serviceCallCompletedLatch;
      m_serviceCallInterrupted = serviceCallInterrupted;
    }

    /**
     * Cancels the {@link RunMonitor} of the request and ensures the service call to be interrupted
     */
    private String cancel() throws Exception {
      m_pingRunMonitor.cancel(true); // Cancel the ping request (do not cancel Future to wait for the result or exception).

      try {
        return m_pingFuture.awaitDoneAndGet(); // Wait for the ping request to return.
      }
      finally {
        assertTrue(m_serviceCallCompletedLatch.await()); // ensure the service call to be completed (server side)
        assertTrue(m_serviceCallInterrupted.get()); // ensure the service call to be interrupted (server side)
      }
    }

    /**
     * Lets the service operation complete.
     */
    private String complete() throws Exception {
      m_serviceCallLatch.unblock();
      try {
        return m_pingFuture.awaitDoneAndGet();
      }
      finally {
        assertTrue(m_serviceCallCompletedLatch.await()); // ensure the service call to be completed (server side)
        assertFalse(m_serviceCallInterrupted.get()); // ensure the service call NOT to be interrupted (server side)
      }
    }
  }
}
