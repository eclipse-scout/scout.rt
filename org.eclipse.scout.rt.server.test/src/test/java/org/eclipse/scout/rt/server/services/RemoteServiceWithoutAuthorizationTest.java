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
package org.eclipse.scout.rt.server.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.server.ServiceOperationInvoker;
import org.eclipse.scout.rt.server.services.common.ping.PingService;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceWithoutAuthorization;
import org.junit.Test;

public class RemoteServiceWithoutAuthorizationTest {

  @Test
  public void testMustAuthorize() throws Exception {
    ServiceOperationInvokerMock bo = new ServiceOperationInvokerMock();
    //
    assertMustAuthorize(bo, IMockProcessService.class, Object.class.getMethod("hashCode"), IMockProcessService.class);
    //
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), IMockProcessService.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), IMockProcessService.class);
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), IMockProcessService.class);
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), IMockProcessService.class);
    //
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), AbstractMockProcessService.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), AbstractMockProcessService.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), AbstractMockProcessService.class);
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), AbstractMockProcessService.class);
    //
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService1.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService1.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService1.class);
    assertMustAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService1.class);
    //
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService2.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService2.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService2.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService2.class);
    //
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService2Sub.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService2Sub.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService2Sub.class);
    assertNoAuthorize(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService2Sub.class);
    //
    assertMustAuthorize(bo, IPingService.class, IPingService.class.getMethod("ping", String.class), PingService.class);

    assertMustAuthorize(bo, IMockChildProcessService.class, IMockChildProcessService.class.getMethod("interna1"), MockChildProcessService.class);
  }

  private static void assertMustAuthorize(ServiceOperationInvokerMock bo, Class<?> serviceInterfaceClass, Method serviceOp, Class<?> serviceImplClass) throws Exception {
    assertTrue(bo.test(serviceInterfaceClass, serviceOp, serviceImplClass));
  }

  private static void assertNoAuthorize(ServiceOperationInvokerMock bo, Class<?> serviceInterfaceClass, Method serviceOp, Class<?> serviceImplClass) throws Exception {
    assertFalse(bo.test(serviceInterfaceClass, serviceOp, serviceImplClass));
  }

  @IgnoreBean
  static class ServiceOperationInvokerMock extends ServiceOperationInvoker {

    public ServiceOperationInvokerMock() {
      super();
    }

    public boolean test(Class<?> interfaceClass, Method interfaceMethod, Class<?> implClass) throws Exception {
      return mustAuthorize(interfaceClass, implClass, interfaceMethod, new Object[0]);
    }
  }

  interface IMockProcessService extends IService {
    void hello();

    @RemoteServiceWithoutAuthorization
    void interna1();

    void interna2();

    void interna3();
  }

  interface IMockChildProcessService extends IMockProcessService {
    void internal4();

    @Override
    void interna1();
  }

  class MockChildProcessService implements IMockChildProcessService {

    @Override
    public void hello() {
    }

    @Override
    public void interna1() {
    }

    @Override
    public void interna2() {
    }

    @Override
    public void interna3() {
    }

    @Override
    public void internal4() {
    }
  }

  abstract class AbstractMockProcessService implements IMockProcessService {

    @Override
    public void hello() {
    }

    @Override
    public void interna1() {
    }

    @RemoteServiceWithoutAuthorization
    @Override
    public void interna2() {
    }
  }

  class MockProcessService1 extends AbstractMockProcessService {

    @Override
    public void hello() {
    }

    @Override
    public void interna2() {
    }

    @Override
    public void interna3() {
    }
  }

  @RemoteServiceWithoutAuthorization
  class MockProcessService2 extends AbstractMockProcessService {

    @Override
    public void hello() {
    }

    @Override
    public void interna3() {
    }
  }

  class MockProcessService2Sub extends MockProcessService2 {

    @RemoteServiceWithoutAuthorization
    @Override
    public void hello() {
    }

    @Override
    public void interna3() {
    }
  }

}
