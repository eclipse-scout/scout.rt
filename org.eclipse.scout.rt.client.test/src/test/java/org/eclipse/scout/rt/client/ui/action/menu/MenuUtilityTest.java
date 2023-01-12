/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 6.1
 */
public class MenuUtilityTest {

  private IContextMenuOwner m_contextMenuOwner;

  @Before
  public void before() {
    m_contextMenuOwner = mock(IContextMenuOwner.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMenuByClassNullContextMenuOwner() {
    MenuUtility.getMenuByClass(null, null);
  }

  @Test
  public void testGetMenuByClassNullMenuType() {
    TestContextMenu contextMenu = new TestContextMenu(mock(IWidget.class), Collections.<IMenu> emptyList());
    when(m_contextMenuOwner.getContextMenu()).thenReturn(contextMenu);
    assertNull(MenuUtility.getMenuByClass(m_contextMenuOwner, null));
  }

  @Test
  public void testGetMenuByClassOwnerHasNoContextMenu() {
    when(m_contextMenuOwner.getContextMenu()).thenReturn(null);
    assertNull(MenuUtility.getMenuByClass(m_contextMenuOwner, TestMenu.class));
  }

  @Test
  public void testGetMenuByClassMenuDoesNotExist() {
    TestContextMenu contextMenu = new TestContextMenu(mock(IWidget.class), Collections.<IMenu> emptyList());
    when(m_contextMenuOwner.getContextMenu()).thenReturn(contextMenu);
    assertNull(MenuUtility.getMenuByClass(m_contextMenuOwner, TestMenu.class));
  }

  @Test
  public void testGetMenuByClassMenuExists() {
    TestMenu menu = new TestMenu();
    TestContextMenu contextMenu = new TestContextMenu(mock(IWidget.class), Collections.singletonList(menu));
    when(m_contextMenuOwner.getContextMenu()).thenReturn(contextMenu);
    assertSame(menu, MenuUtility.getMenuByClass(m_contextMenuOwner, TestMenu.class));
  }

  private static class TestContextMenu extends AbstractContextMenu<IWidget> {
    public TestContextMenu(IWidget owner, List<? extends IMenu> initialChildList) {
      super(owner, initialChildList);
    }

    @Override
    protected boolean isOwnerPropertyChangedListenerRequired() {
      return false;
    }
  }

  private static class TestMenu extends AbstractMenu {
  }
}
