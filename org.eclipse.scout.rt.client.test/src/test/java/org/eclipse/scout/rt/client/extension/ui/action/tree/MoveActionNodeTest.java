/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.action.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top1Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top1Menu.Sub1Top1Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top1Menu.Sub1Top1Menu.Sub1Sub1Top1Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top1Menu.Sub2Top1Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top2Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top2Menu.Sub1Top2Menu;
import org.eclipse.scout.rt.client.extension.ui.action.tree.fixture.TestMenus.Top2Menu.Sub2Top2Menu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IMoveModelObjectToRootMarker;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.junit.Test;

public class MoveActionNodeTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() {
    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top1Menu.class, Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub1Top1Menu.class, Sub2Top1Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions().get(0).getChildActions(), Sub1Sub1Top1Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions(), Sub1Top2Menu.class, Sub2Top2Menu.class);
  }

  @Test
  public void testMoveTopLevelMenu() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Menu.class, 5);
    doTestMoveTopLevelMenu();
  }

  @Test
  public void testMoveTopLevelMenuMoveToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Top2Menu.class, 5d);
    doTestMoveTopLevelMenu();
  }

  @Test
  public void testMoveTopLevelMenuMoveWithIMoveModelObjectToRootMarker() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Menu.class, 5d, IMoveModelObjectToRootMarker.class);
    doTestMoveTopLevelMenu();
  }

  @Test
  public void testMoveTopLevelMenuMoveExplicitNullParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top2Menu.class, 5d, null);
    doTestMoveTopLevelMenu();
  }

  protected void doTestMoveTopLevelMenu() {
    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top2Menu.class, Top1Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub1Top2Menu.class, Sub2Top2Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions(), Sub1Top1Menu.class, Sub2Top1Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions().get(0).getChildActions(), Sub1Sub1Top1Menu.class);
  }

  @Test
  public void testMoveSubMenuWithinSameParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Menu.class, 30);
    doTestMoveSubMenuWithinSameParent();
  }

  @Test
  public void testMoveSubMenuWithinSameParentExplicitNullParent() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Menu.class, 30d, null);
    doTestMoveSubMenuWithinSameParent();
  }

  @Test
  public void testMoveSubMenuWithinSameParentExplicitParentClass() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Menu.class, 30d, Top1Menu.class);
    doTestMoveSubMenuWithinSameParent();
  }

  protected void doTestMoveSubMenuWithinSameParent() {
    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top1Menu.class, Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub2Top1Menu.class, Sub1Top1Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions().get(1).getChildActions(), Sub1Sub1Top1Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions(), Sub1Top2Menu.class, Sub2Top2Menu.class);
  }

  @Test
  public void testMoveSubMenuToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(Sub1Top1Menu.class, 15d);
    doTestMoveSubMenuToRoot();
  }

  @Test
  public void testMoveSubMenuToRootWithIMoveModelObjectToRootMarker() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Menu.class, 15d, IMoveModelObjectToRootMarker.class);
    doTestMoveSubMenuToRoot();
  }

  protected void doTestMoveSubMenuToRoot() {
    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top1Menu.class, Sub1Top1Menu.class, Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub2Top1Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions(), Sub1Sub1Top1Menu.class);
    assertMenus(topLevelMenu.get(2).getChildActions(), Sub1Top2Menu.class, Sub2Top2Menu.class);
  }

  @Test
  public void testMoveRootMenuToSubMenu() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Menu.class, 15d, Top2Menu.class);

    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub1Top2Menu.class, Top1Menu.class, Sub2Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions().get(1).getChildActions(), Sub1Top1Menu.class, Sub2Top1Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions().get(1).getChildActions().get(0).getChildActions(), Sub1Sub1Top1Menu.class);
  }

  @Test
  public void testMoveMenuToAnotherMenuWithouChangingOrder() {
    BEANS.get(IExtensionRegistry.class).registerMove(Sub1Top1Menu.class, null, Top2Menu.class);

    TestMenus menus = new TestMenus();
    List<IMenu> topLevelMenu = menus.getMenus();
    assertMenus(topLevelMenu, Top1Menu.class, Top2Menu.class);
    assertMenus(topLevelMenu.get(0).getChildActions(), Sub2Top1Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions(), Sub1Top1Menu.class, Sub1Top2Menu.class, Sub2Top2Menu.class);
    assertMenus(topLevelMenu.get(1).getChildActions().get(0).getChildActions(), Sub1Sub1Top1Menu.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveMenuToItselfExceptionOnRegisterMove() {
    BEANS.get(IExtensionRegistry.class).registerMove(Top1Menu.class, null, Top1Menu.class);
    new TestMenus();
  }

  protected static void assertMenus(List<IMenu> menus, Class<?>... expectedMenuClasses) {
    assertEquals(expectedMenuClasses.length, CollectionUtility.size(menus));

    for (int i = 0; i < expectedMenuClasses.length; i++) {
      assertSame(expectedMenuClasses[i], menus.get(i).getClass());
    }
  }
}
