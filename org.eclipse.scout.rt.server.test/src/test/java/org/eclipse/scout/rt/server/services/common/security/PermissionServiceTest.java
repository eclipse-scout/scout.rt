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
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.security.Permission;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission1;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission2;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link IPermissionService}
 */
@RunWith(PlatformTestRunner.class)
public class PermissionServiceTest {

  /* ---------------------------------------------------------------------------------------------- */
  /* Tests for Bug 398323 - CodeService / PermissionService: More fine-grained lookup strategies for finding classes */
  /* ---------------------------------------------------------------------------------------------- */

  private void testImpl(IPermissionService testService, boolean testPermission1Expected, boolean testPermission2Expected) {
    List<IBean<?>> reg = TestingUtility.registerBeans(
        new BeanMetaData(IPermissionService.class)
            .withInitialInstance(testService)
            .withApplicationScoped(true));
    try {
      IPermissionService service = BEANS.get(IPermissionService.class);
      assertSame(testService, service);
      //
      Set<Class<? extends Permission>> result = service.getAllPermissionClasses();
      boolean testPermission1Found = false;
      boolean testPermission2Found = false;
      for (Class<?> b : result) {
        if (ObjectUtility.equals(b.getName(), TestPermission1.class.getName())) {
          testPermission1Found = true;
        }
        if (ObjectUtility.equals(b.getName(), TestPermission2.class.getName())) {
          testPermission2Found = true;
        }
      }
      //
      if (testPermission1Expected) {
        assertTrue("TestPermission1 class not found (expected: found)", testPermission1Found);
      }
      else {
        assertFalse("TestPermission1 class found (expected: not found)", testPermission1Found);
      }
      if (testPermission2Expected) {
        assertTrue("TestPermission2 class not found (expected: found)", testPermission2Found);
      }
      else {
        assertFalse("TestPermission2 class found (expected: not found)", testPermission2Found);
      }
    }
    finally {
      TestingUtility.unregisterBeans(reg);
    }
  }

  @Test
  public void testDefault() {
    testImpl(new PermissionService_Default_Mock(), true, true);
  }

  @Test
  public void testIgnoreClassName() {
    testImpl(new PermissionService_IgnoreClassName1_Mock(), false, true);
  }

  @Test
  public void testIgnoreClass() {
    testImpl(new PermissionService_IgnoreClass2_Mock(), true, false);
  }

  abstract static class AbstractPermissionServiceMock extends PermissionService {

    public AbstractPermissionServiceMock() {
      super();
    }

  }

  static class PermissionService_Default_Mock extends AbstractPermissionServiceMock {

    public PermissionService_Default_Mock() {
      super();
    }
  }

  static class PermissionService_IgnoreClassName1_Mock extends AbstractPermissionServiceMock {

    public PermissionService_IgnoreClassName1_Mock() {
      super();
    }

    @Override
    protected boolean acceptClassName(String className) {
      return super.acceptClassName(className) && ObjectUtility.notEquals(className, TestPermission1.class.getName());
    }
  }

  static class PermissionService_IgnoreClass2_Mock extends AbstractPermissionServiceMock {

    public PermissionService_IgnoreClass2_Mock() {
      super();
    }

    @Override
    protected boolean acceptClass(IClassInfo ci) {
      return super.acceptClass(ci) && (!ci.name().equals(TestPermission2.class.getName()));
    }
  }
}
