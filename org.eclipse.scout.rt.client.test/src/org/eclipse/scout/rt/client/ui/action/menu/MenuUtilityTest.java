/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.junit.Test;

/**
 * Tests for {@link MenuUtility}
 * 
 * @since 3.10.0-M4
 */
public class MenuUtilityTest {
  @Test
  public void testGetKeyStrokesFromMenus() {
    IMenu menu = EasyMock.createNiceMock(IMenu.class);
    EasyMock.expect(menu.getKeyStroke()).andReturn("f1");
    EasyMock.replay(menu);
    IMenu[] menus = new IMenu[]{menu};
    IKeyStroke[] keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    assertEquals("should return 1 menu", keyStrokes.length, 1);
    assertEquals("keyStroke should be f11", keyStrokes[0].getKeyStroke(), "f1");

    keyStrokes = MenuUtility.getKeyStrokesFromMenus(null);
    assertNotNull(keyStrokes);
    assertEquals("null Parameter should return empty array", keyStrokes.length, 0);

    menu = EasyMock.createNiceMock(IMenu.class);
    EasyMock.expect(menu.getKeyStroke()).andReturn(" "); //invalid keyStroke definition
    EasyMock.replay(menu);
    menus = new IMenu[]{menu};
    keyStrokes = MenuUtility.getKeyStrokesFromMenus(menus);
    assertNotNull(keyStrokes);
    assertEquals("KeyStrokes should be empty, since ' ' is an invalid keyStroke", keyStrokes.length, 0);
  }

  @Test
  public void testFilterValidMenusValueFieldDisabled() {
    @SuppressWarnings("unchecked")
    IValueField<String> valueField = (IValueField<String>) EasyMock.createMock(IValueField.class);
    EasyMock.expect(valueField.getValue()).andReturn("testValue");
    EasyMock.expect(valueField.isEnabled()).andReturn(false);

    IMenu menu = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menu.isInheritAccessibility()).andReturn(true);
    EasyMock.replay(valueField, menu);

    IMenu[] menus = new IMenu[]{menu};
    IMenu[] filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false);

    assertNotNull(filteredMenus);
    assertEquals(0, filteredMenus.length);
  }

  @Test
  public void testFilterValidMenusInheritAccessibilityAndSingleSelectionMenu() {
    @SuppressWarnings("unchecked")
    IValueField<String> valueField = (IValueField<String>) EasyMock.createMock(IValueField.class);
    EasyMock.expect(valueField.getValue()).andReturn("testValue").times(1);
    EasyMock.expect(valueField.getValue()).andReturn(null).times(1);
    EasyMock.expect(valueField.isEnabled()).andReturn(false).anyTimes();

    IMenu menu = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menu.isInheritAccessibility()).andReturn(false).anyTimes();
    EasyMock.expect(menu.isEmptySpaceAction()).andReturn(false).anyTimes();
    EasyMock.expect(menu.isSingleSelectionAction()).andReturn(true).anyTimes();
    EasyMock.expect(menu.isVisible()).andReturn(true).anyTimes();
    EasyMock.replay(valueField, menu);

    IMenu[] menus = new IMenu[]{menu};
    IMenu[] filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false);
    assertNotNull(filteredMenus);
    assertEquals(1, filteredMenus.length);
    assertEquals(menu, filteredMenus[0]);

    filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false); // value of field is null
    assertNotNull(filteredMenus);
    assertEquals(0, filteredMenus.length);
  }

  @Test
  public void testFilterValidMenusEmptySpaceActionMenu() {
    @SuppressWarnings("unchecked")
    IValueField<String> valueField = (IValueField<String>) EasyMock.createMock(IValueField.class);
    EasyMock.expect(valueField.getValue()).andReturn("testValue").anyTimes();
    EasyMock.expect(valueField.isEnabled()).andReturn(true).anyTimes();

    IMenu menuWithEmptySpaceAction = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menuWithEmptySpaceAction.isInheritAccessibility()).andReturn(false).anyTimes();
    EasyMock.expect(menuWithEmptySpaceAction.isEmptySpaceAction()).andReturn(true).anyTimes();
    EasyMock.expect(menuWithEmptySpaceAction.isVisible()).andReturn(true).times(1);
    EasyMock.expect(menuWithEmptySpaceAction.isVisible()).andReturn(false).times(2);
    EasyMock.replay(valueField, menuWithEmptySpaceAction);

    IMenu[] menus = new IMenu[]{menuWithEmptySpaceAction};
    IMenu[] filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false);
    assertNotNull(filteredMenus);
    assertEquals(1, filteredMenus.length);
    assertEquals(menuWithEmptySpaceAction, filteredMenus[0]);

    filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false);
    assertNotNull(filteredMenus);
    assertEquals(0, filteredMenus.length);
  }

  @Test
  public void testFilterValidMenusPrepareAction() {
    @SuppressWarnings("unchecked")
    IValueField<String> valueField = (IValueField<String>) EasyMock.createMock(IValueField.class);
    EasyMock.expect(valueField.getValue()).andReturn("testValue").anyTimes();
    EasyMock.expect(valueField.isEnabled()).andReturn(true).anyTimes();

    final IMenu menuWithEmptySpaceAction = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menuWithEmptySpaceAction.isInheritAccessibility()).andReturn(false).anyTimes();
    EasyMock.expect(menuWithEmptySpaceAction.isEmptySpaceAction()).andReturn(true).anyTimes();
    EasyMock.expect(menuWithEmptySpaceAction.isVisible()).andReturn(true).anyTimes();

    menuWithEmptySpaceAction.prepareAction();
    EasyMock.expectLastCall().once();
    EasyMock.replay(valueField, menuWithEmptySpaceAction);

    IMenu[] menus = new IMenu[]{menuWithEmptySpaceAction};
    IMenu[] filteredMenus = MenuUtility.filterValidMenus(valueField, menus, true);
    assertNotNull(filteredMenus);
    assertEquals(1, filteredMenus.length);
    assertEquals(menuWithEmptySpaceAction, filteredMenus[0]);
    EasyMock.verify(menuWithEmptySpaceAction);

    filteredMenus = MenuUtility.filterValidMenus(valueField, menus, false);
    EasyMock.verify(menuWithEmptySpaceAction);
  }

  @Test
  public void testFilterValidMenusOnButton() {
    IButton button = EasyMock.createMock(IButton.class);
    final IMenu menu = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menu.isVisible()).andReturn(true).times(1);
    EasyMock.expect(menu.isVisible()).andReturn(false).times(1);
    EasyMock.replay(button, menu);

    IMenu[] menus = new IMenu[]{menu};
    IMenu[] filteredMenus = MenuUtility.filterValidMenusOnButton(button, menus, false);
    assertNotNull(filteredMenus);
    assertEquals(1, filteredMenus.length);
    assertEquals(menu, filteredMenus[0]);

    filteredMenus = MenuUtility.filterValidMenusOnButton(button, menus, false);
    assertNotNull(filteredMenus);
    assertEquals(0, filteredMenus.length);
  }

  @Test
  public void testFilterValidMenusOnButtonPrepareAction() {
    IButton button = EasyMock.createMock(IButton.class);
    final IMenu menu = EasyMock.createMock(IMenu.class);
    EasyMock.expect(menu.isVisible()).andReturn(true).anyTimes();

    menu.prepareAction();
    EasyMock.expectLastCall().once();
    EasyMock.replay(button, menu);

    IMenu[] menus = new IMenu[]{menu};
    IMenu[] filteredMenus = MenuUtility.filterValidMenusOnButton(button, menus, true);
    assertNotNull(filteredMenus);
    assertEquals(1, filteredMenus.length);
    assertEquals(menu, filteredMenus[0]);
    EasyMock.verify(menu);

    filteredMenus = MenuUtility.filterValidMenusOnButton(button, menus, false);
    EasyMock.verify(menu);
  }

}
