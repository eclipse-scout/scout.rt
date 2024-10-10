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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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

  @Test
  public void testGetPublicMethod() {
    ServiceOperationInvoker invoker = new ServiceOperationInvoker();
    assertNull(invoker.getPublicMethod(Fixture.class, "privateMethod", int.class));
    assertNull(invoker.getPublicMethod(Fixture.class, "defaultMethod", long.class));
    assertNull(invoker.getPublicMethod(Fixture.class, "protectedMethod", float.class));
    runTestGetPublicMethod(invoker, Fixture.class, "publicMethod", double.class);

    assertNull(invoker.getPublicMethod(SubFixture.class, "privateSubMethod", int.class));
    assertNull(invoker.getPublicMethod(SubFixture.class, "defaultSubMethod", long.class));
    assertNull(invoker.getPublicMethod(SubFixture.class, "protectedSubMethod", float.class));
    runTestGetPublicMethod(invoker, SubFixture.class, "publicMethod", double.class);
    runTestGetPublicMethod(invoker, SubFixture.class, "publicSubMethod", double.class);
  }

  protected void runTestGetPublicMethod(ServiceOperationInvoker invoker, Class clazz, String methodName, Class paramType) {
    Method m = invoker.getPublicMethod(clazz, methodName, paramType);
    assertEquals(methodName, m.getName());
    assertEquals(paramType, m.getParameterTypes()[0]);
    assertEquals(void.class, m.getReturnType());
  }

  @Test
  public void testGetAllDeclaredMethods() {
    ServiceOperationInvoker invoker = new ServiceOperationInvoker();
    Method[] methods = invoker.getPublicMethods(Fixture.class);
    // NOTE: do not assert method count and exact method set since test framework agents could add additional methods (e.g. $jacocoInit method used for profiling)
    assertTrue(CollectionUtility.containsAll(Arrays.stream(methods).map(Method::getName).collect(Collectors.toSet()), Set.of("publicMethod")));
    assertSame(methods, invoker.getPublicMethods(Fixture.class));

    Method[] methodsSub = invoker.getPublicMethods(SubFixture.class);
    // NOTE: do not assert method count and exact method set since test framework agents could add additional methods (e.g. $jacocoInit method used for profiling)
    assertTrue(CollectionUtility.containsAll(Arrays.stream(methodsSub).map(Method::getName).collect(Collectors.toSet()), Set.of("publicSubMethod", "publicMethod")));
    assertSame(methodsSub, invoker.getPublicMethods(SubFixture.class));
  }

  @SuppressWarnings("unused")
  private static class Fixture {
    private void privateMethod(int i) {
    }

    void defaultMethod(long l) {
    }

    protected void protectedMethod(float f) {
    }

    public void publicMethod(double d) {
    }
  }


  @SuppressWarnings("unused")
  private static class SubFixture extends Fixture {
    private void privateSubMethod(int i) {
    }

    void defaultSubMethod(long l) {
    }

    protected void protectedSubMethod(float f) {
    }

    public void publicSubMethod(double d) {
    }

    @Override
    public void publicMethod(double d) {
      // overridden method fixture
      super.publicMethod(d);
    }
  }
}
