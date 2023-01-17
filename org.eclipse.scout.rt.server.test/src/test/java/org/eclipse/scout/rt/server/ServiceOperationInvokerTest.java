/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ServiceOperationInvoker}
 *
 * @author jgu
 */
@RunWith(PlatformTestRunner.class)
public class ServiceOperationInvokerTest {

  @BeanMock
  private IPingService m_pingSvc;

  private String m_testData = "hello";

  @Test
  public void testInvokeWithSession() {
    when(m_pingSvc.ping(any(String.class))).thenReturn(m_testData);
    ServiceTunnelResponse res = invokePingService(createRunContextWithSession());
    assertValidResponse(res, m_testData);
  }

  @Test
  public void testInvokeInspectedWithSession() {
    try {
      BEANS.get(ProcessInspector.class).setEnabled(true);
      when(m_pingSvc.ping(any(String.class))).thenReturn(m_testData);
      ServiceTunnelResponse res = invokePingService(createRunContextWithSession());
      assertValidResponse(res, m_testData);
    }
    finally {
      BEANS.get(ProcessInspector.class).setEnabled(false);
    }
  }

  @Test(expected = ProcessingException.class) //exception is handled with JUnitExceptionHandler
  public void testInvokeInvalidWithSession() {
    String exceptionMessage = "xxx";
    when(m_pingSvc.ping(any(String.class))).thenThrow(new ProcessingException(exceptionMessage));
    ServiceTunnelResponse res = invokePingService(createRunContextWithSession());
    assertProcessingException(res, exceptionMessage);
  }

  @Test
  public void testInvokeWithoutSession() {
    when(m_pingSvc.ping(any(String.class))).thenReturn(m_testData);
    ServiceTunnelResponse res = invokePingService(ServerRunContexts.empty());
    assertValidResponse(res, m_testData);
  }

  @Test(expected = ProcessingException.class) //exception is handled with JUnitExceptionHandler
  public void testInvokeInvalidWithoutSession() {
    String exceptionMessage = "xxx";
    when(m_pingSvc.ping(any(String.class))).thenThrow(new ProcessingException(exceptionMessage));
    ServiceTunnelResponse res = invokePingService(ServerRunContexts.empty());
    assertProcessingException(res, exceptionMessage);
  }

  /**
   * Asserts that the response contains an exception without the customMessage information (security)
   */
  private void assertProcessingException(ServiceTunnelResponse res, String customMessage) {
    Throwable exception = res.getException();
    MatcherAssert.assertThat(exception, instanceOf(ProcessingException.class));
    MatcherAssert.assertThat("Exception message should not reveil anything (security)", exception.getMessage(), not(containsString(customMessage)));
    assertEquals("Stacktrace must be empty (security)", 0, exception.getStackTrace().length);
    assertNotNull(res.getProcessingDuration());
  }

  private ServerRunContext createRunContextWithSession() {
    return ServerRunContexts
        .empty()
        .withSession(new TestServerSession());
  }

  private ServiceTunnelResponse invokePingService(final ServerRunContext runcontext) {
    ServiceOperationInvoker s = new ServiceOperationInvoker();
    ServiceTunnelRequest request = new ServiceTunnelRequest(IPingService.class.getName(), "ping", new Class[]{String.class}, new Object[]{"hello"});
    return s.invoke(runcontext, request);
  }

  private void assertValidResponse(ServiceTunnelResponse res, String data) {
    assertNull(res.getException());
    assertNotNull(res.getProcessingDuration());
    assertEquals(data, res.getData());
  }

}
