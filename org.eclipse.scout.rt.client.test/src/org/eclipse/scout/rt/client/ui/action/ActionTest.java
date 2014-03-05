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
package org.eclipse.scout.rt.client.ui.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield.MainBox.SmartField1;
import org.eclipse.scout.rt.client.ui.action.fixture.TestFormWithTemplateSmartfield.MainBox.SmartField2;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link AbstractAction}
 * 
 * @since 3.8.2
 */
public class ActionTest {
  private static final String TEST_CLASS_ID = "TEST_CLASS_ID";

  @Test
  public void testOutlineButton() throws ProcessingException {
    IDesktop desktopMock = Mockito.mock(IDesktop.class);
    IOutline outlineMock = Mockito.mock(IOutline.class);

    Mockito.when(desktopMock.getAvailableOutlines()).thenReturn(CollectionUtility.arrayList(outlineMock));
    final IntegerHolder execActionHolder = new IntegerHolder(0);
    final IntegerHolder execToggleHolder = new IntegerHolder(0);
    AbstractOutlineViewButton b = new AbstractOutlineViewButton(desktopMock, outlineMock.getClass()) {
      @Override
      protected void execAction() throws ProcessingException {
        execActionHolder.setValue(execActionHolder.getValue() + 1);
      }

      @Override
      protected void execToggleAction(boolean selected) throws ProcessingException {
        execToggleHolder.setValue(execToggleHolder.getValue() + 1);
      }
    };
    b.getUIFacade().fireActionFromUI();
    assertEquals(1, execActionHolder.getValue().intValue());
    assertEquals(1, execToggleHolder.getValue().intValue());
    assertTrue(b.isSelected());

    b.getUIFacade().fireActionFromUI();
    assertEquals(1, execActionHolder.getValue().intValue());
    assertEquals(1, execToggleHolder.getValue().intValue());
    assertTrue(b.isSelected());

    b.setSelected(false);
    assertEquals(1, execActionHolder.getValue().intValue());
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
  public void testActionClassIds() throws ProcessingException {
    assertEquals(TEST_CLASS_ID, new AnnotatedAction().classId());
  }

  /**
   * Test for {@link AbstractMenu#classId()} when using smartfields and templates
   */
  @Test
  public void testActionClassIdsForTemplates() throws ProcessingException {
    TestFormWithTemplateSmartfield smartfield = new TestFormWithTemplateSmartfield();
    List<IMenu> menus1 = smartfield.getFieldByClass(SmartField1.class).getMenus();
    List<IMenu> menus2 = smartfield.getFieldByClass(SmartField2.class).getMenus();
    if (menus1.size() != 1 || menus2.size() != 1) {
      fail("Test smartfields should contain exactly one menu.");
    }

    assertNotEquals(CollectionUtility.firstElement(menus1).classId(), CollectionUtility.firstElement(menus2).classId());
  }

  @ClassId(TEST_CLASS_ID)
  static class AnnotatedAction extends AbstractAction {
  }

  public static class BaseAction extends AbstractAction {
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
}
