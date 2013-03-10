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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.annotations.IOrdered;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.extension.client.IWrappedObject;
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
    Assert.assertNull(MenuExtensionUtility.getAnchorType(null));
    Assert.assertNull(MenuExtensionUtility.getAnchorType("string is an unsupported anchor type"));
    //
    Assert.assertSame(IPage.class, MenuExtensionUtility.getAnchorType(
        new AbstractPageWithNodes() {
        }));
    //
    Assert.assertSame(IFormField.class, MenuExtensionUtility.getAnchorType(
        new AbstractSmartField<Long>() {
        }));
    //
    Assert.assertSame(IFormField.class, MenuExtensionUtility.getAnchorType(
        new AbstractButton() {
        }));
    //
    Assert.assertSame(IMenu.class, MenuExtensionUtility.getAnchorType(
        new AbstractMenu() {
        }));
    //
    Assert.assertSame(IMenu.class, MenuExtensionUtility.getAnchorType(
        new AbstractExtensibleMenu() {
        }));
  }

  @Test
  public void testContributeMenus() throws Exception {
    P_AnchorNodePage anchor = new P_AnchorNodePage();
    // for node pages, anchor and container are the same
    P_AnchorNodePage container = anchor;
    List<IMenu> menuList = new ArrayList<IMenu>();
    //
    MenuExtensionUtility.contributeMenus(anchor, container, null, menuList, false);
    Assert.assertTrue(menuList.isEmpty());
    //
    MenuExtensionUtility.contributeMenus(anchor, container, Collections.<MenuContributionExtension> emptyList(), menuList, false);
    Assert.assertTrue(menuList.isEmpty());
    //
    menuList.add(m_a);
    menuList.add(m_b);
    menuList.add(m_c);
    MenuExtensionUtility.contributeMenus(anchor, container, Collections.<MenuContributionExtension> emptyList(), menuList, false);
    Assert.assertEquals(Arrays.asList(m_a, m_b, m_c), menuList);
    //
    List<MenuContributionExtension> menuExtensions = new ArrayList<MenuContributionExtension>();
    menuExtensions.add(new MenuContributionExtension(P_ADynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menuList, false);
    Assert.assertEquals(4, menuList.size());
    Assert.assertSame(m_a, menuList.get(0));
    Assert.assertTrue(menuList.get(1) instanceof P_ADynamicMenu);
    Assert.assertSame(m_b, menuList.get(2));
    Assert.assertSame(m_c, menuList.get(3));
    //
    menuList.clear();
    menuList.add(m_a);
    menuList.add(m_c);
    menuList.add(m_b);
    menuExtensions.add(0, new MenuContributionExtension(P_BDynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 100d));
    menuExtensions.add(new MenuContributionExtension(P_CDynamicMenu.class, new MenuAnchorFilter(P_OtherNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menuList, false);
    Assert.assertEquals(5, menuList.size());
    Assert.assertSame(m_a, menuList.get(0));
    Assert.assertTrue(menuList.get(1) instanceof P_ADynamicMenu);
    Assert.assertSame(m_b, menuList.get(2));
    Assert.assertSame(m_c, menuList.get(3));
    Assert.assertTrue(menuList.get(4) instanceof P_BDynamicMenu);
  }

  @Test
  public void testContributeWrappedMenus() throws Exception {
    P_AnchorNodePage anchor = new P_AnchorNodePage();
    // for node pages, anchor and container are the same
    P_AnchorNodePage container = anchor;
    List<IMenu> menuList = new ArrayList<IMenu>();
    //
    menuList.add(m_a);
    menuList.add(m_b);
    menuList.add(m_c);
    List<MenuContributionExtension> menuExtensions = new ArrayList<MenuContributionExtension>();
    menuExtensions.add(new MenuContributionExtension(P_ADynamicMenu.class, new MenuAnchorFilter(P_AnchorNodePage.class), 15d));
    MenuExtensionUtility.contributeMenus(anchor, container, menuExtensions, menuList, true);
    Assert.assertEquals(4, menuList.size());
    Assert.assertSame(m_a, menuList.get(0));
    //
    IMenu dynamicMenu = menuList.get(1);
    Assert.assertTrue(dynamicMenu instanceof IMenu);
    Assert.assertTrue(dynamicMenu instanceof IWrappedObject);
    Assert.assertTrue(dynamicMenu instanceof IOrdered);
    Assert.assertFalse(dynamicMenu instanceof P_ADynamicMenu);
    @SuppressWarnings("unchecked")
    IMenu wrappedMeny = ((IWrappedObject<IMenu>) dynamicMenu).getWrappedObject();
    Assert.assertNotNull(wrappedMeny);
    Assert.assertTrue(wrappedMeny instanceof P_ADynamicMenu);
    //
    Assert.assertSame(m_b, menuList.get(2));
    Assert.assertSame(m_c, menuList.get(3));
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
