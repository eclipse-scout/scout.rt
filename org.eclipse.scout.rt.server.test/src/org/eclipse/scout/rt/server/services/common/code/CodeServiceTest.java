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
package org.eclipse.scout.rt.server.services.common.code;

import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType2;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Test for {@link ICodeService}
 */
@RunWith(ScoutServerTestRunner.class)
public class CodeServiceTest {

  /* ---------------------------------------------------------------------------------------------- */
  /* Tests for Bug 398323 - CodeService / PermissionService: More fine-grained lookup strategies for finding classes */
  /* ---------------------------------------------------------------------------------------------- */

  private void testImpl(ICodeService testService, boolean testCodeType1Expected, boolean testCodeType2Expected) {
    List<ServiceRegistration> reg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, testService);
    try {
      ICodeService service = SERVICES.getService(ICodeService.class);
      Assert.assertSame(testService, service);
      //
      BundleClassDescriptor[] result = service.getAllCodeTypeClasses("");
      boolean testCodeType1Found = false;
      boolean testCodeType2Found = false;
      for (BundleClassDescriptor b : result) {
        if (CompareUtility.equals(b.getClassName(), TestCodeType1.class.getName())) {
          testCodeType1Found = true;
        }
        if (CompareUtility.equals(b.getClassName(), TestCodeType2.class.getName())) {
          testCodeType2Found = true;
        }
      }
      //
      if (testCodeType1Expected) {
        Assert.assertTrue("TestCodeType1 class not found (expected: found)", testCodeType1Found);
      }
      else {
        Assert.assertFalse("TestCodeType1 class found (expected: not found)", testCodeType1Found);
      }
      if (testCodeType2Expected) {
        Assert.assertTrue("TestCodeType2 class not found (expected: found)", testCodeType2Found);
      }
      else {
        Assert.assertFalse("TestCodeType2 class found (expected: not found)", testCodeType2Found);
      }
    }
    finally {
      TestingUtility.unregisterServices(reg);
    }
  }

  @Test
  public void testDefault() throws ProcessingException {
    testImpl(new CodeService_Default_Mock(), true, true);
  }

  @Test
  public void testIgnoreBundle() throws ProcessingException {
    testImpl(new CodeService_IgnoreThisBundle_Mock(), false, false);
  }

  @Test
  public void testIgnoreClassName() throws ProcessingException {
    testImpl(new CodeService_IgnoreClassName1_Mock(), false, true);
  }

  @Test
  public void testIgnoreClass() throws ProcessingException {
    testImpl(new CodeService_IgnoreClass2_Mock(), true, false);
  }

  abstract static class AbstractCodeServiceMock extends CodeService {

    public AbstractCodeServiceMock() throws ProcessingException {
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

  static class CodeService_Default_Mock extends AbstractCodeServiceMock {

    public CodeService_Default_Mock() throws ProcessingException {
      super();
    }
  }

  static class CodeService_IgnoreThisBundle_Mock extends AbstractCodeServiceMock {

    public CodeService_IgnoreThisBundle_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptBundle(Bundle bundle, String classPrefix) {
      return super.acceptBundle(bundle, classPrefix) && (bundle != Activator.getDefault().getBundle());
    }
  }

  static class CodeService_IgnoreClassName1_Mock extends AbstractCodeServiceMock {

    public CodeService_IgnoreClassName1_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClassName(Bundle bundle, String className) {
      return super.acceptClassName(bundle, className) && CompareUtility.notEquals(className, TestCodeType1.class.getName());
    }
  }

  static class CodeService_IgnoreClass2_Mock extends AbstractCodeServiceMock {

    public CodeService_IgnoreClass2_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClass(Bundle bundle, Class<?> c) {
      return super.acceptClass(bundle, c) && (c != TestCodeType2.class);
    }
  }
}
