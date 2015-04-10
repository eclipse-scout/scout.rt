/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.fixture.MockServerProcessingCancelService;
import org.eclipse.scout.rt.client.fixture.MockServiceTunnel;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests what happens when the user cancels a long running job (system=false) that is caused by a backend service call
 * through the client proxy.
 */
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@RunWithSubject("anna")
public class ClientJobCancelTest {
  private static long pingServiceDelay;

  private List<IBean<?>> m_serviceReg;
  private TestEnvironmentClientSession m_session;
  private static IClientServiceTunnel oldServiceTunnel;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    IClientSession clientSession = ClientSessionProvider.currentSession();
    if (clientSession != null) {
      oldServiceTunnel = clientSession.getServiceTunnel();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TestEnvironmentClientSession clientSession = (TestEnvironmentClientSession) ClientSessionProvider.currentSession();
    clientSession.setServiceTunnel(oldServiceTunnel);
  }

  @Before
  public void setUp() throws Exception {
    m_session = ClientSessionProvider.currentSession(TestEnvironmentClientSession.class);
    MockServiceTunnel tunnel = new MockServiceTunnel(m_session);
    m_session.setServiceTunnel(tunnel);
    m_serviceReg = TestingUtility.registerBeans(
        new BeanMetaData(MockPingService.class).
        initialInstance(new MockPingService()).
        applicationScoped(true),
        new BeanMetaData(MockServerProcessingCancelService.class).
        initialInstance(new MockServerProcessingCancelService(tunnel)).
        applicationScoped(true)
        );
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterBeans(m_serviceReg);
    m_session = null;
  }

  @Test
  public void testPingWithoutDelayAndInterrupt() throws Exception {
    String s = testInternal(10L, false);
    assertEquals("pong", s);
  }

  @Test
  public void testPingWithDelayAndInterruptClientJob() throws Exception {
    String s = testInternal(1000000L, true);
    Assert.assertNull(s); // we don't expect an answer
  }

  /**
   * run a client job, let the service call be delayed and optionally interrupt after one second
   */
  protected String testInternal(long delay, boolean interrupt) throws Exception {
    pingServiceDelay = delay;
    //
    IFuture<String> future = ClientJobs.schedule(new ICallable<String>() {
      @Override
      public String call() throws Exception {
        IPingService serviceProxy = ServiceTunnelUtility.createProxy(IPingService.class, m_session.getServiceTunnel());
        return serviceProxy.ping("ABC");
      }
    }, ClientJobs.newInput(ClientRunContexts.copyCurrent().session(m_session)));

    //make user interrupt the job in 1 sec
    if (interrupt) {
      ClientJobs.schedule(new JobThatInterrupts(future), 1, TimeUnit.SECONDS, ClientJobs.newInput(ClientRunContexts.copyCurrent().session(m_session)));
    }
    //wait for user job
    return future.awaitDoneAndGet();
  }

  private static class JobThatInterrupts implements IRunnable {
    private IFuture<?> m_jobToInterrupt;

    private JobThatInterrupts(IFuture<?> jobToInterrupt) {
      m_jobToInterrupt = jobToInterrupt;
    }

    @Override
    public void run() throws Exception {
      m_jobToInterrupt.cancel(true);
    }
  }

  private static class MockPingService extends AbstractService implements IPingService {
    @Override
    public String ping(String s) {
      try {
        Thread.sleep(pingServiceDelay);
      }
      catch (InterruptedException e) {
        System.out.println("Interrupted");
      }
      return "pong";
    }
  }
}
