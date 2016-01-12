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
package org.eclipse.scout.rt.server;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
  IPingService m_pingSvc;

  @Test
  public void testInvokeWithSession() {
    when(m_pingSvc.ping(any(String.class))).thenReturn("hello");
    ServiceTunnelResponse res = invokePingService(createRunContextWithSession());
    assertNull(res.getException());
    assertNotNull(res.getProcessingDuration());
    assertEquals("hello", res.getData());
  }

  @Test(expected = ProcessingException.class) //exception is handled with JUnitExceptionHandler
  public void testInvokeInvalidWithSession() {
    when(m_pingSvc.ping(any(String.class))).thenThrow(new ProcessingException("xxx"));
    ServiceTunnelResponse res = invokePingService(createRunContextWithSession());
    Throwable exception = res.getException();
    assertThat(exception, instanceOf(ProcessingException.class));
    assertThat(exception.getMessage(), not(containsString("xxx")));
    assertNotNull(res.getProcessingDuration());
  }

  private ServerRunContext createRunContextWithSession() {
    return ServerRunContexts
        .empty()
        .withSession(new TestServerSession());
  }

  private ServiceTunnelResponse invokePingService(final ServerRunContext runcontext) {
    return runcontext.call(new Callable<ServiceTunnelResponse>() {
      @Override
      public ServiceTunnelResponse call() {
        ServiceOperationInvoker s = new ServiceOperationInvoker();
        ServiceTunnelRequest request = new ServiceTunnelRequest(IPingService.class.getName(), "ping", new Class[]{String.class}, new Object[]{"hello"});
        return s.invoke(request);
      }
    });
  }

}
