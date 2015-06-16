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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType2;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ICodeService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class CodeServiceTest {

  /* ---------------------------------------------------------------------------------------------- */
  /* Tests for Bug 398323 - CodeService / PermissionService: More fine-grained lookup strategies for finding classes */
  /* ---------------------------------------------------------------------------------------------- */

  private void testImpl(ICodeService testService, boolean testCodeType1Expected, boolean testCodeType2Expected) {
    List<IBean<?>> reg = TestingUtility.registerBeans(new BeanMetaData(ICodeService.class).initialInstance(testService).applicationScoped(true).addAnnotation(AnnotationFactory.createOrder(-1000)));
    try {
      ICodeService service = BEANS.get(ICodeService.class);
      assertSame(testService, service);
      //
      Collection<Class<? extends ICodeType<?, ?>>> result = service.getAllCodeTypeClasses("");
      boolean testCodeType1Found = false;
      boolean testCodeType2Found = false;
      for (Class<? extends ICodeType<?, ?>> b : result) {
        if (CompareUtility.equals(b.getName(), TestCodeType1.class.getName())) {
          testCodeType1Found = true;
        }
        if (CompareUtility.equals(b.getName(), TestCodeType2.class.getName())) {
          testCodeType2Found = true;
        }
      }
      //
      if (testCodeType1Expected) {
        assertTrue("TestCodeType1 class not found (expected: found)", testCodeType1Found);
      }
      else {
        assertFalse("TestCodeType1 class found (expected: not found)", testCodeType1Found);
      }
      if (testCodeType2Expected) {
        assertTrue("TestCodeType2 class not found (expected: found)", testCodeType2Found);
      }
      else {
        assertFalse("TestCodeType2 class found (expected: not found)", testCodeType2Found);
      }
    }
    finally {
      TestingUtility.unregisterBeans(reg);
    }
  }

  @Test
  public void testDefault() throws ProcessingException {
    testImpl(new CodeService_Default_Mock(), true, true);
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

  }

  static class CodeService_Default_Mock extends AbstractCodeServiceMock {

    public CodeService_Default_Mock() throws ProcessingException {
      super();
    }
  }

  static class CodeService_IgnoreClassName1_Mock extends AbstractCodeServiceMock {

    public CodeService_IgnoreClassName1_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClassName(String name) {
      return super.acceptClassName(name) && CompareUtility.notEquals(name, TestCodeType1.class.getName());
    }
  }

  static class CodeService_IgnoreClass2_Mock extends AbstractCodeServiceMock {

    public CodeService_IgnoreClass2_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClass(IClassInfo ci) {
      return super.acceptClass(ci) && (!ci.name().equals(TestCodeType2.class.getName()));
    }
  }

//TODO aho/jgu adapt to new client notification
//  /* ---------------------------------------------------------------------------------------------- */
//  /* Tests for Bug 444213 - Test that IClientNotificationService is called in unloadCodeTypeCache() */
//  /* ---------------------------------------------------------------------------------------------- */
//
//  @Test
//  public void testReloadCodeType() throws Exception {
//    IClientNotificationService clientNotificationService = Mockito.mock(IClientNotificationService.class);
//    List<IBean<?>> reg = TestingUtility.registerBeans(
//        new BeanMetaData(IClientNotificationService.class).
//            initialInstance(clientNotificationService).
//            applicationScoped(true));
//    try {
//      CodeService codeService = new CodeService();
//
//      codeService.reloadCodeType(SomeCodeType.class);
//
//      ArgumentCaptor<CodeTypeChangedNotification> notification = ArgumentCaptor.forClass(CodeTypeChangedNotification.class);
//      ArgumentCaptor<IClientNotificationFilter> filter = ArgumentCaptor.forClass(IClientNotificationFilter.class);
//      Mockito.verify(clientNotificationService).putNotification(notification.capture(), filter.capture());
//
//      assertEquals("CodeType list in the notification size", 1, notification.getValue().getCodeTypes().size());
//      assertEquals("CodeType list(0) class", SomeCodeType.class, notification.getValue().getCodeTypes().get(0));
//    }
//    finally {
//      TestingUtility.unregisterBeans(reg);
//    }
//  }
//
//  @Test
//  public void testReloadCodeTypes() throws Exception {
//    IClientNotificationService clientNotificationService = Mockito.mock(IClientNotificationService.class);
//    List<IBean<?>> reg = TestingUtility.registerBeans(
//        new BeanMetaData(IClientNotificationService.class).
//            initialInstance(clientNotificationService).
//            applicationScoped(true));
//    try {
//      CodeService codeService = new CodeService();
//
//      List<Class<? extends ICodeType<?, ?>>> list = new ArrayList<Class<? extends ICodeType<?, ?>>>();
//      list.add(SomeCodeType.class);
//      list.add(DummyCodeType.class);
//      codeService.reloadCodeTypes(list);
//
//      ArgumentCaptor<CodeTypeChangedNotification> notification = ArgumentCaptor.forClass(CodeTypeChangedNotification.class);
//      ArgumentCaptor<IClientNotificationFilter> filter = ArgumentCaptor.forClass(IClientNotificationFilter.class);
//      Mockito.verify(clientNotificationService).putNotification(notification.capture(), filter.capture());
//
//      assertEquals("CodeType list in the notification size", 2, notification.getValue().getCodeTypes().size());
//      assertEquals("CodeType list(0) class", SomeCodeType.class, notification.getValue().getCodeTypes().get(0));
//      assertEquals("CodeType list(1) class", DummyCodeType.class, notification.getValue().getCodeTypes().get(1));
//    }
//    finally {
//      TestingUtility.unregisterBeans(reg);
//    }
//  }

  public static class SomeCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 100L;
    }

  }

  public static class DummyCodeType extends AbstractCodeType<Long, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 500L;
    }
  }
}
