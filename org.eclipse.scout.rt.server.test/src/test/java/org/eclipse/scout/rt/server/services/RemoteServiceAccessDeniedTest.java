/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.security.Permissions;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.server.ServiceOperationInvoker;
import org.eclipse.scout.rt.server.services.common.ping.PingService;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.junit.Test;

public class RemoteServiceAccessDeniedTest {

  @Test
  public void testAnnotations() throws Exception {
    ServiceOperationInvokerMock bo = new ServiceOperationInvokerMock();
    //
    assertNonAccessible(bo, IMockProcessService.class, Object.class.getMethod("hashCode"), IMockProcessService.class);
    //
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), IMockProcessService.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), IMockProcessService.class);
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), IMockProcessService.class);
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), IMockProcessService.class);
    //
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), AbstractMockProcessService.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), AbstractMockProcessService.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), AbstractMockProcessService.class);
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), AbstractMockProcessService.class);
    //
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService1.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService1.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService1.class);
    assertAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService1.class);
    //
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService2.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService2.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService2.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService2.class);
    //
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("hello"), MockProcessService2Sub.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna1"), MockProcessService2Sub.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna2"), MockProcessService2Sub.class);
    assertNonAccessible(bo, IMockProcessService.class, IMockProcessService.class.getMethod("interna3"), MockProcessService2Sub.class);
    //
    assertAccessible(bo, IPingService.class, IPingService.class.getMethod("ping", String.class), PingService.class);

    assertNonAccessible(bo, IMockChildProcessService.class, IMockChildProcessService.class.getMethod("interna1"), MockChildProcessService.class);
  }

  private static void assertAccessible(ServiceOperationInvokerMock bo, Class<?> serviceInterfaceClass, Method serviceOp, Class<?> serviceImplClass) throws Exception {
    bo.test(serviceInterfaceClass, serviceOp, serviceImplClass);
  }

  private static void assertNonAccessible(ServiceOperationInvokerMock bo, Class<?> serviceInterfaceClass, Method serviceOp, Class<?> serviceImplClass) throws Exception {
    try {
      bo.test(serviceInterfaceClass, serviceOp, serviceImplClass);
    }
    catch (SecurityException e) {
      return;
    }
    fail("should fail");
  }

  @IgnoreBean
  static class ServiceOperationInvokerMock extends ServiceOperationInvoker {
    private final Permissions m_permissionCollection;

    public ServiceOperationInvokerMock() {
      super();
      m_permissionCollection = new Permissions();
      m_permissionCollection.add(new RemoteServiceAccessPermission("*.shared.*", "*"));
      m_permissionCollection.add(new RemoteServiceAccessPermission("*.IMockProcessService", "*"));
      m_permissionCollection.add(new RemoteServiceAccessPermission("*.INonTunnelProcessService", "*"));
    }

    public void test(Class<?> interfaceClass, Method interfaceMethod, Class<?> implClass) throws Exception {
      checkRemoteServiceAccessByInterface(interfaceClass, interfaceMethod, new Object[0]);
      checkRemoteServiceAccessByAnnotations(interfaceClass, implClass, interfaceMethod, new Object[0]);
      checkRemoteServiceAccessByPermission(interfaceClass, implClass, interfaceMethod, new Object[0]);
    }

    @Override
    protected void checkRemoteServiceAccessByPermission(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
      if (!m_permissionCollection.implies(new RemoteServiceAccessPermission(interfaceClass.getName(), interfaceMethod.getName()))) {
        throw new SecurityException("no access");
      }
    }
  }

  interface IMockProcessService extends IService {
    void hello();

    @RemoteServiceAccessDenied
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

    @RemoteServiceAccessDenied
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

  @RemoteServiceAccessDenied
  class MockProcessService2 extends AbstractMockProcessService {

    @Override
    public void hello() {
    }

    @Override
    public void interna3() {
    }
  }

  class MockProcessService2Sub extends MockProcessService2 {

    @RemoteServiceAccessDenied
    @Override
    public void hello() {
    }

    @Override
    public void interna3() {
    }
  }

}
