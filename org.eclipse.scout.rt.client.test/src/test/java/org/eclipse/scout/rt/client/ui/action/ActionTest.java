/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension;
import org.eclipse.scout.rt.client.extension.ui.action.ActionChains.ActionDisposeChain;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.ActionTest.MenuInheritanceTestForm.MainBox.MyBigDecimalField;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield.MainBox.SmartField1;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield.MainBox.SmartField2;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link AbstractAction}
 *
 * @since 3.8.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ActionTest {
  private static final String TEST_ACTION_CLASS_ID = "TEST_CLASS_ID";

  @Test
  public void testOutlineButton() {
    IDesktop desktopMock = Mockito.mock(IDesktop.class);
    IOutline outlineMock = Mockito.mock(IOutline.class);

    Mockito.when(desktopMock.getAvailableOutlines()).thenReturn(CollectionUtility.arrayList(outlineMock));
    final IntegerHolder execActionHolder = new IntegerHolder(0);
    final IntegerHolder execToggleHolder = new IntegerHolder(0);
    AbstractOutlineViewButton b = new AbstractOutlineViewButton(desktopMock, outlineMock.getClass()) {
      @Override
      protected void execAction() {
        execActionHolder.setValue(execActionHolder.getValue() + 1);
      }

      @Override
      protected void execSelectionChanged(boolean selection) {
        execToggleHolder.setValue(execToggleHolder.getValue() + 1);
      }
    };
    b.getUIFacade().setSelectedFromUI(true);
    b.getUIFacade().fireActionFromUI();
    assertEquals(1, execActionHolder.getValue().intValue());
    assertEquals(1, execToggleHolder.getValue().intValue());
    assertTrue(b.isSelected());

    b.getUIFacade().fireActionFromUI();
    assertEquals(2, execActionHolder.getValue().intValue());
    assertEquals(1, execToggleHolder.getValue().intValue());
    assertTrue(b.isSelected());

    b.getUIFacade().setSelectedFromUI(false);
    b.getUIFacade().fireActionFromUI();
    assertEquals(3, execActionHolder.getValue().intValue());
    assertEquals(2, execToggleHolder.getValue().intValue());
    assertFalse(b.isSelected());

  }

  @Test
  public void testGetFieldId() {
    assertEquals("BaseAction", new BaseAction().getActionId());
    assertEquals("BaseAction", new ExtendedTestAction().getActionId());
    assertEquals("ExtendedTestActionWithoutReplace", new ExtendedTestActionWithoutReplace().getActionId());
    //
    assertEquals("Custom", new TestActionWithCustomActionId().getActionId());
    assertEquals("Custom", new ExtendedTestActionWithCustomActionId().getActionId());
  }

  @Test
  public void testActionClassIds() {
    assertEquals(TEST_ACTION_CLASS_ID, new AnnotatedAction().classId());
  }

  /**
   * test for {@link AbstractAction#combineKeyStrokes(String...)}
   */
  @Test
  public void testCombineKeyStrokes() {
    assertEquals(IKeyStroke.CONTROL + '-' + IKeyStroke.F1, AbstractAction.combineKeyStrokes(IKeyStroke.CONTROL, IKeyStroke.F1));
  }

  /**
   * Test for {@link AbstractMenu#classId()} when using smartfields and templates
   */
  @Test
  public void testActionClassIdsForTemplates() {
    TestFormWithTemplateSmartfield smartfield = new TestFormWithTemplateSmartfield();
    List<IMenu> menus1 = smartfield.getFieldByClass(SmartField1.class).getMenus();
    List<IMenu> menus2 = smartfield.getFieldByClass(SmartField2.class).getMenus();
    if (menus1.size() != 1 || menus2.size() != 1) {
      fail("Test smartfields should contain exactly one menu.");
    }

    assertNotEquals(CollectionUtility.firstElement(menus1).classId(), CollectionUtility.firstElement(menus2).classId());
  }

  @Test
  public void testKeystroke() {
    BaseAction action = new BaseAction();
    assertNull(action.getKeyStroke());
    action.setKeyStroke("");
    assertNull(action.getKeyStroke());
    action.setKeyStroke(null);
    assertNull(action.getKeyStroke());
    action.setKeyStroke("f11");
    assertEquals("f11", action.getKeyStroke());
  }

  public static class BaseActionExtension extends AbstractActionExtension<BaseAction> {

    public BaseActionExtension(BaseAction owner) {
      super(owner);
    }

    @Override
    public void execDispose(ActionDisposeChain chain) {
      chain.execDispose();
      getOwner().additionalDispose();
    }
  }

  @Test
  public void testDisposeAction() {
    BEANS.get(IExtensionRegistry.class).register(BaseActionExtension.class);
    try {
      BaseAction action = new BaseAction();
      action.dispose();
      assertTrue(action.m_disposeInternalCalled);
      assertTrue(action.m_execDisposeCalled);
      assertTrue(action.m_additionalDisposeCalled);
    }
    finally {
      BEANS.get(IExtensionRegistry.class).deregister(BaseActionExtension.class);
    }
  }

  @Test
  public void testActionInheritAccessibility() {
    MenuInheritanceTestForm f = new MenuInheritanceTestForm();
    f.start();
    MyBigDecimalField menuOwner = f.getFieldByClass(MenuInheritanceTestForm.MainBox.MyBigDecimalField.class);
    IAction menu = menuOwner.getMenus().get(0);
    // validate setup
    Assert.assertTrue(menu.isInheritAccessibility());
    Assert.assertTrue(menuOwner.isEnabled());
    Assert.assertFalse(menuOwner.isEnabledIncludingParents());
    Assert.assertFalse(f.getRootGroupBox().isEnabled());

    // check that the menu inherits the state of the field
    Assert.assertFalse(menu.isEnabledIncludingParents());
  }

  @ClassId(TEST_ACTION_CLASS_ID)
  static class AnnotatedAction extends AbstractAction {
  }

  public static class BaseAction extends AbstractAction {
    boolean m_disposeInternalCalled;
    boolean m_execDisposeCalled;
    boolean m_additionalDisposeCalled;

    @Override
    protected void disposeActionInternal() {
      super.disposeActionInternal();
      m_disposeInternalCalled = true;
    }

    public void additionalDispose() {
      m_additionalDisposeCalled = true;
    }

    @Override
    protected void execDispose() {
      super.execDispose();
      m_execDisposeCalled = true;
    }
  }

  @Replace
  public static class ExtendedTestAction extends BaseAction {
  }

  public static class ExtendedTestActionWithoutReplace extends BaseAction {
  }

  public static class TestActionWithCustomActionId extends AbstractAction {
    @Override
    public String getActionId() {
      return "Custom";
    }
  }

  @Replace
  public static class ExtendedTestActionWithCustomActionId extends TestActionWithCustomActionId {
  }

  public static class MenuInheritanceTestForm extends AbstractForm {

    public class MainBox extends AbstractGroupBox {
      @Override
      protected boolean getConfiguredEnabled() {
        return false;
      }

      public class MyBigDecimalField extends AbstractBigDecimalField {
        public class MyMenuMenu extends AbstractMenu {
        }
      }
    }
  }
}
