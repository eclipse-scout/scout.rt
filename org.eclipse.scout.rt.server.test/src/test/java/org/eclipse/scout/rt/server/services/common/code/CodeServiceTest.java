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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.services.common.code.fixture.IgnoredCodeType;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.server.services.common.code.fixture.TestCodeType2;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test for {@link ICodeService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class CodeServiceTest {
  private ClientNotificationRegistry m_clientNotificationReg;
  private List<IBean<?>> m_reg;

  @Before
  public void before() {
    m_clientNotificationReg = Mockito.mock(ClientNotificationRegistry.class);
    m_reg = TestingUtility.registerBeans(new BeanMetaData(ClientNotificationRegistry.class).withInitialInstance(m_clientNotificationReg).withApplicationScoped(true));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_reg);
  }

  /**
   * Tests that a code type class is found by {@link ICodeService#getAllCodeTypeClasses(String)}
   */
  @Test
  public void testCodeTypeClasses() {
    ICodeService service = BEANS.get(ICodeService.class);
    Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = service.getAllCodeTypeClasses("");
    assertTrue(allCodeTypes.contains(TestCodeType1.class));
  }

  /**
   * Tests that a Codetype with Annotation {@link IgnoreBean} is not found
   */
  @Test
  public void testCodeTypeClass_IgnoredNotFound() {
    ICodeService service = BEANS.get(ICodeService.class);
    Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = service.getAllCodeTypeClasses("");
    assertFalse(allCodeTypes.contains(IgnoredCodeType.class));
  }

  /**
   * Tests that a Codetype that is not instanciable is not found
   */
  @Test
  public void testCodeTypeClass_AbstractNotFound() {
    ICodeService service = BEANS.get(ICodeService.class);
    Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = service.getAllCodeTypeClasses("");
    assertFalse(allCodeTypes.contains(AbstractCodeType.class));
  }

  @Test
  public void testCodeTypeClass_IgnoredName() throws ProcessingException {
    CodeService_IgnoreClassName1_Mock testService = new CodeService_IgnoreClassName1_Mock();
    List<IBean<?>> reg = TestingUtility.registerBeans(new BeanMetaData(ICodeService.class).withInitialInstance(testService).withApplicationScoped(true));
    try {
      ICodeService service = BEANS.get(ICodeService.class);
      Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = service.getAllCodeTypeClasses("");
      assertFalse(allCodeTypes.contains(TestCodeType1.class));
      assertTrue(allCodeTypes.contains(TestCodeType2.class));
    }
    finally {
      TestingUtility.unregisterBeans(reg);
    }
  }

  static class CodeService_IgnoreClassName1_Mock extends CodeService {

    public CodeService_IgnoreClassName1_Mock() throws ProcessingException {
      super();
    }

    @Override
    protected boolean acceptClassName(String name) {
      return super.acceptClassName(name) && CompareUtility.notEquals(name, TestCodeType1.class.getName());
    }
  }

  /**
   * Tests that a client notification is created when reloading a code type {@link CodeService#reloadCodeType}
   *
   * @throws ProcessingException
   */
  @Test
  public void testReloadCodeType() throws ProcessingException {
    CodeService codeService = new CodeService();
    codeService.reloadCodeType(SomeCodeType.class);

    ArgumentCaptor<CodeTypeChangedNotification> notification = ArgumentCaptor.forClass(CodeTypeChangedNotification.class);
    verify(m_clientNotificationReg).putTransactionalForAllSessions(notification.capture());

    assertEquals("CodeType set in the notification size", 1, notification.getValue().getCodeTypes().size());
    assertTrue(notification.getValue().getCodeTypes().contains(SomeCodeType.class));
  }

  @Test
  public void testReloadCodeTypes() throws ProcessingException {
    CodeService codeService = new CodeService();

    List<Class<? extends ICodeType<?, ?>>> list = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    list.add(SomeCodeType.class);
    list.add(DummyCodeType.class);
    codeService.reloadCodeTypes(list);

    ArgumentCaptor<CodeTypeChangedNotification> notification = ArgumentCaptor.forClass(CodeTypeChangedNotification.class);
    verify(m_clientNotificationReg).putTransactionalForAllSessions(notification.capture());

    assertEquals("CodeType list in the notification size", 2, notification.getValue().getCodeTypes().size());
    assertTrue(notification.getValue().getCodeTypes().contains(SomeCodeType.class));
    assertTrue(notification.getValue().getCodeTypes().contains(DummyCodeType.class));
  }

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
