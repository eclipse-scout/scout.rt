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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.OutlineTreeContextMenuTest.PageWithTable.Table.PageWithTableEmptySpace2Menu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.OutlineTreeContextMenuTest.PageWithTable.Table.PageWithTableEmptySpaceMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.OutlineTreeContextMenuTest.PageWithTable.Table.PageWithTableRowMenu;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Creates an outline consisting of a PageWithTable and PageWithNode as child. Tests if menus appear on parent
 * as well on client node correctly.
 */
@RunWith(ScoutClientTestRunner.class)
public class OutlineTreeContextMenuTest {

  @Test
  public void testEmptySpaceAndRowMenus() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithTableOutline.class, outline.getClass());

    IPage page = outline.getActivePage();
    assertNotNull(page);
    assertSame(PageWithTable.class, page.getClass());

    assertEmptySpaceMenusExistOnTablePageParent(outline);
    assertRowMenusExistOnTablePageNode(outline);
  }

  private static void assertEmptySpaceMenusExistOnTablePageParent(IOutline outline) throws Exception {
    outline.selectFirstNode();

    ITreeNode selectedNode = outline.getSelectedNode();
    List<IMenu> menus = selectedNode.getTree().getUIFacade().fireNodePopupFromUI();

    @SuppressWarnings("unchecked")
    List<IMenu> requiredMenus = resolveMenusOfActiveTablePage(outline, PageWithTableEmptySpaceMenu.class, PageWithTableEmptySpace2Menu.class);

    assertTrue(menus.containsAll(requiredMenus));
    assertTrue(menus.size() == (requiredMenus.size()));
  }

  private static void assertRowMenusExistOnTablePageNode(IOutline outline) throws Exception {
    outline.selectFirstNode();

    @SuppressWarnings("unchecked")
    List<IMenu> requiredMenus = resolveMenusOfActiveTablePage(outline, PageWithTableRowMenu.class);

    outline.selectNextChildNode();

    ITreeNode selectedNode = outline.getSelectedNode();
    List<IMenu> menus = selectedNode.getTree().getUIFacade().fireNodePopupFromUI();

    assertTrue(menus.containsAll(requiredMenus));
    assertTrue(menus.size() == (requiredMenus.size()));
  }

  private static List<IMenu> resolveMenusOfActiveTablePage(IOutline outline, Class<? extends IMenu>... menuClasses) throws Exception {
    List<IMenu> resolvedMenus = new LinkedList<IMenu>();
    IPageWithTable<?> activePage = (IPageWithTable<?>) outline.getActivePage();

    for (Class<? extends IMenu> menuClass : menuClasses) {
      IMenu menu = activePage.getTable().getMenu(menuClass);
      assertNotNull(menu);

      resolvedMenus.add(menu);
    }

    return resolvedMenus;
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{new Object[]{"a", "b"}};
    }

    @Override
    protected IPage execCreateChildPage(ITableRow row) throws ProcessingException {
      return new PageWithNode();
    }

    public class Table extends AbstractTable {

      @Order(10.0)
      public class PageWithTableEmptySpaceMenu extends AbstractMenu {

        @Override
        protected boolean getConfiguredEmptySpaceAction() {
          return true;
        }

        @Override
        protected boolean getConfiguredSingleSelectionAction() {
          return false;
        }

        @Override
        protected String getConfiguredText() {
          return "EmptySpace";
        }

      }

      @Order(15.0)
      public class PageWithTableEmptySpace2Menu extends AbstractMenu {

        @Override
        protected boolean getConfiguredSingleSelectionAction() {
          return false;
        }

        @Override
        protected String getConfiguredText() {
          return "EmptySpace 2";
        }

      }

      @Order(20.0)
      public class PageWithTableRowMenu extends AbstractMenu {

        @Override
        protected boolean getConfiguredSingleSelectionAction() {
          return true;
        }

        @Override
        protected String getConfiguredText() {
          return "Edit";
        }

      }

      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
      }
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {

  }
}
