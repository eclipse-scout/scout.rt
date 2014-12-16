/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.action.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuAnchorFilter;
import org.eclipse.scout.rt.extension.client.ui.action.menu.internal.MenuContributionExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.9.0
 */
public class MenuExtensionUtilityTest {

  private P_AMenu m_a;
  private P_BMenu m_b;
  private P_CMenu m_c;

  @Before
  public void before() {
    m_a = new P_AMenu();
    m_b = new P_BMenu();
    m_c = new P_CMenu();
  }

  @Test
  public void testGetAnchorType() {
    assertNull(MenuExtensionUtility.getAnchorType(null));
    assertNull(MenuExtensionUtility.getAnchorType("string is an unsupported anchor type"));
    //
    assertSame(IPage.class, MenuExtensionUtility.getAnchorType(
        new AbstractPageWithNodes() {
        }));
    //
    assertSame(IFormField.class, MenuExtensionUtility.getAnchorType(
        new AbstractSmartField<Long>() {
        }));
    //
    assertSame(IFormField.class, MenuExtensionUtility.getAnchorType(
        new AbstractButton() {
        }));
    //
    assertSame(IMenu.class, MenuExtensionUtility.getAnchorType(
        new AbstractMenu() {
        }));
    //
    assertSame(IMenu.class, MenuExtensionUtility.getAnchorType(
        new AbstractExtensibleMenu() {
        }));
  }

  @Test
  public void testContributeMenus() throws Exception {
    P_AnchorNodePage anchor = new P_AnchorNodePage();
    // for node pages, anchor and container are the same
    P_AnchorNodePage container = anchor;
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    //
    MenuExtensionUtility.contributeMenus(anchor, container, null, menus);
    assertTrue(menus.isEmpty());
    //
    MenuExtensionUtility.contributeMenus(anchor, container, Collections.<MenuContributionExtension> emptyList(), menus);
    assertTrue(menus.isEmpty());
    //
    menus.addOrdered(m_a);
    menus.addOrdered(m_b);
    menus.addOrdered(m_c);
    MenuExtensionUtility.contributeMenus(anchor, container, Collections.<MenuContributionExtension> emptyList(), menus);
    assertEquals(Arrays.asList(m_a, m_b, m_c), menus.getOrderedList());
    //
    List<MenuContributionExtension> menuExtensions = new ArrayList<MenuContributionExtension>();
    menuExtensions.add(new MenuContributionExtension(P_ADynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menus);
    assertEquals(4, menus.size());
    assertSame(m_a, menus.get(0));
    assertTrue(menus.get(1) instanceof P_ADynamicMenu);
    assertSame(m_b, menus.get(2));
    assertSame(m_c, menus.get(3));
    //
    menus.clear();
    menus.addOrdered(m_a);
    menus.addOrdered(m_c);
    menus.addOrdered(m_b);
    menuExtensions.add(0, new MenuContributionExtension(P_BDynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 100d));
    menuExtensions.add(new MenuContributionExtension(P_CDynamicMenu.class, new MenuAnchorFilter(P_OtherNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menus);
    assertEquals(5, menus.size());
    assertSame(m_a, menus.get(0));
    assertTrue(menus.get(1) instanceof P_ADynamicMenu);
    assertSame(m_b, menus.get(2));
    assertSame(m_c, menus.get(3));
    assertTrue(menus.get(4) instanceof P_BDynamicMenu);
  }

  @Test
  public void testContributeWrappedMenus() throws Exception {
    P_AnchorNodePage anchor = new P_AnchorNodePage();
    // for node pages, anchor and container are the same
    P_AnchorNodePage container = anchor;
    OrderedCollection<IMenu> menuList = new OrderedCollection<IMenu>();
    //
    menuList.addOrdered(m_a);
    menuList.addOrdered(m_b);
    menuList.addOrdered(m_c);
    List<MenuContributionExtension> menuExtensions = new ArrayList<MenuContributionExtension>();
    menuExtensions.add(new MenuContributionExtension(P_ADynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menuList);
    assertEquals(4, menuList.size());
    assertSame(m_a, menuList.get(0));
    //
    IMenu dynamicMenu = menuList.get(1);
    assertTrue(dynamicMenu instanceof IMenu);
    assertTrue(dynamicMenu instanceof IOrdered);
    assertTrue(dynamicMenu instanceof P_ADynamicMenu);
    Assert.assertEquals(15d, dynamicMenu.getOrder(), 0.0);
    assertSame(m_b, menuList.get(2));
    assertSame(m_c, menuList.get(3));
  }

  @Order(10)
  public static class P_AMenu extends AbstractMenu {
  }

  @Order(20)
  public static class P_BMenu extends AbstractMenu {
  }

  @Order(30)
  public static class P_CMenu extends AbstractMenu {
  }

  public static class P_ADynamicMenu extends AbstractMenu {
  }

  public static class P_BDynamicMenu extends AbstractMenu {
  }

  public static class P_CDynamicMenu extends AbstractMenu {
  }

  public static class P_AnchorNodePage extends AbstractPageWithNodes {
  }

  public static class P_OtherNodePage extends AbstractPageWithNodes {
  }
}
