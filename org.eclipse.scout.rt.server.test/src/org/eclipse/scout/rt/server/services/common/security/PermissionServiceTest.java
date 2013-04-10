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
package org.eclipse.scout.rt.server.services.common.security;

import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission1;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission2;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Test for {@link IPermissionService}
 */
@RunWith(ScoutServerTestRunner.class)
public class PermissionServiceTest {

  /* ---------------------------------------------------------------------------------------------- */
  /* Tests for Bug 398323 - CodeService / PermissionService: More fine-grained lookup strategies for finding classes */
  /* ---------------------------------------------------------------------------------------------- */

  private void testImpl(IPermissionService testService, boolean testPermission1Expected, boolean testPermission2Expected) {
    List<ServiceRegistration> reg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, testService);
    try {
      IPermissionService service = SERVICES.getService(IPermissionService.class);
      Assert.assertSame(testService, service);
      //
      BundleClassDescriptor[] result = service.getAllPermissionClasses();
      boolean testPermission1Found = false;
      boolean testPermission2Found = false;
      for (BundleClassDescriptor b : result) {
        if (CompareUtility.equals(b.getClassName(), TestPermission1.class.getName())) {
          testPermission1Found = true;
        }
        if (CompareUtility.equals(b.getClassName(), TestPermission2.class.getName())) {
          testPermission2Found = true;
        }
      }
      //
      if (testPermission1Expected) {
        Assert.assertTrue("TestPermission1 class not found (expected: found)", testPermission1Found);
      }
      else {
        Assert.assertFalse("TestPermission1 class found (expected: not found)", testPermission1Found);
      }
      if (testPermission2Expected) {
        Assert.assertTrue("TestPermission2 class not found (expected: found)", testPermission2Found);
      }
      else {
        Assert.assertFalse("TestPermission2 class found (expected: not found)", testPermission2Found);
      }
    }
    finally {
      TestingUtility.unregisterServices(reg);
    }
  }

  @Test
  public void testDefault() throws ProcessingException {
    testImpl(new PermissionService_Default_Mock(), true, true);
  }

  @Test
  public void testIgnoreBundle() throws ProcessingException {
    testImpl(new PermissionService_IgnoreThisBundle_Mock(), false, false);
  }

  @Test
  public void testIgnoreClassName() throws ProcessingException {
    testImpl(new PermissionService_IgnoreClassName1_Mock(), false, true);
  }

  @Test
  public void testIgnoreClass() throws ProcessingException {
    testImpl(new PermissionService_IgnoreClass2_Mock(), true, false);
  }

  abstract static class AbstractPermissionServiceMock extends PermissionService {

    public AbstractPermissionServiceMock() throws ProcessingException {
      super();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initializeService() {
    }

    @Override
    public void initializeService(ServiceRegistration registration) {
    }
  }

  static class PermissionService_Default_Mock extends AbstractPermissionServiceMock {

    public PermissionService_Default_Mock() throws ProcessingException {
      super();
    }
  }

  static class PermissionService_IgnoreThisBundle_Mock extends AbstractPermissionServiceMock {

    public PermissionService_IgnoreThisBundle_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptBundle(Bundle bundle) {
      return super.acceptBundle(bundle) && (bundle != Activator.getDefault().getBundle());
    }
  }

  static class PermissionService_IgnoreClassName1_Mock extends AbstractPermissionServiceMock {

    public PermissionService_IgnoreClassName1_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClassName(Bundle bundle, String className) {
      return super.acceptClassName(bundle, className) && CompareUtility.notEquals(className, TestPermission1.class.getName());
    }
  }

  static class PermissionService_IgnoreClass2_Mock extends AbstractPermissionServiceMock {

    public PermissionService_IgnoreClass2_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClass(Bundle bundle, Class<?> c) {
      return super.acceptClass(bundle, c) && (c != TestPermission2.class);
    }
  }
}
