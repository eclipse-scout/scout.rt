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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.fixture.MockServerProcessingCancelService;
import org.eclipse.scout.rt.client.fixture.MockServiceTunnel;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests what happens when the user cancels a long running job (system=false) that is caused by a backend service call
 * through the client proxy.
 */
public class ClientJobCancelTest {
  private static long pingServiceDelay;

  private List<ServiceRegistration> m_serviceReg;
  private TestEnvironmentClientSession m_session;
  private static IClientServiceTunnel oldServiceTunnel;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TestEnvironmentClientSession clientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(TestEnvironmentClientSession.class, UserAgent.createDefault());
    if (clientSession != null) {
      oldServiceTunnel = clientSession.getServiceTunnel();
    }
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TestEnvironmentClientSession clientSession = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(TestEnvironmentClientSession.class, UserAgent.createDefault());
    clientSession.setServiceTunnel(oldServiceTunnel);
  }

  @Before
  public void setUp() throws Exception {
    m_session = SERVICES.getService(IClientSessionRegistryService.class).newClientSession(TestEnvironmentClientSession.class, UserAgent.createDefault());
    MockServiceTunnel tunnel = new MockServiceTunnel(m_session);
    m_session.setServiceTunnel(tunnel);
    m_serviceReg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 0, new MockPingService(), new MockServerProcessingCancelService(tunnel));
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterServices(m_serviceReg);
    m_session = null;
  }

  @Test
  public void testPingWithoutDelayAndInterrupt() throws Exception {
    String s = (String) testInternal(10L, false);
    assertEquals("pong", s);
  }

  @Test
  public void testPingWithDelayAndInterruptClientJob() throws Exception {
    UndeclaredThrowableException u = (UndeclaredThrowableException) testInternal(1000000L, true);
    Throwable t = u.getCause().getCause();
    assertEquals(InterruptedException.class, t.getClass());
    assertEquals(ScoutTexts.get("UserInterrupted"), t.getMessage());
  }

  /**
   * run a client job, let the service call be delayed and optionally interrupt after one second
   */
  protected Object testInternal(long delay, boolean interrupt) throws Exception {
    pingServiceDelay = delay;
    //
    final AtomicReference<Object> resultRef = new AtomicReference<Object>();
    ClientSyncJob job = new ClientSyncJob("Client", m_session) {
      @Override
      protected IStatus runStatus(IProgressMonitor monitor) {
        IPingService serviceProxy = ServiceTunnelUtility.createProxy(IPingService.class, m_session.getServiceTunnel());
        try {
          resultRef.set(serviceProxy.ping("ABC"));
        }
        catch (Throwable t) {
          resultRef.set(t);
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
    //make user interrupt the job in 1 sec
    if (interrupt) {
      new JobThatInterrupts(job.getName()).schedule(1000);
    }
    //wait for user job
    job.join();
    return resultRef.get();
  }

  private class JobThatInterrupts extends Job {
    private final String m_jobToInterrupt;

    public JobThatInterrupts(String jobToInterrupt) {
      super("Interrupter");
      m_jobToInterrupt = jobToInterrupt;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      for (Job j : Job.getJobManager().find(null)) {
        if (m_jobToInterrupt.equals(j.getName())) {
          j.cancel();
        }
      }
      return Status.OK_STATUS;
    }
  }

  public static class MockPingService extends AbstractService implements IPingService {
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
