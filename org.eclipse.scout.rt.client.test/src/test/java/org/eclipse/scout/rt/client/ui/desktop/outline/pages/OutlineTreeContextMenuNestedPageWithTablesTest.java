/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.MenuWrapper;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.OutlineTreeContextMenuNestedPageWithTablesTest.PageWithTable.Table.PageWithTableRowMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.OutlineTreeContextMenuNestedPageWithTablesTest.SubPageWithTable.Table.SubPageWithTableEmptySpaceMenu;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Creates an outline consisting of a PageWithTable and another PageWithTable as child. Tests if menus appear on parent
 * as well on client node correctly.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OutlineTreeContextMenuNestedPageWithTablesTest {

  @Test
  public void testEmptySpaceAndRowMenus() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithTableOutline.class, outline.getClass());

    IPage<?> page = outline.getActivePage();
    assertNotNull(page);
    assertSame(PageWithTable.class, page.getClass());

    assertRowMenusExistOnTablePageNode(outline);
  }

  private static void assertRowMenusExistOnTablePageNode(IOutline outline) {
    outline.selectFirstNode();

    IPageWithTable<?> activePage = (IPageWithTable<?>) outline.getActivePage();
    List<IMenu> requiredMenus = resolveMenusOfPageWithTable(activePage, PageWithTableRowMenu.class);

    outline.selectNextChildNode();

    SubPageWithTable subTablePage = outline.findPage(SubPageWithTable.class);
    requiredMenus.addAll(resolveMenusOfPageWithTable(subTablePage, SubPageWithTableEmptySpaceMenu.class));
    ITreeNode selectedNode = outline.getSelectedNode();
    List<IMenu> menus = selectedNode.getTree().getMenus();
    assertTrue(containsAllMenus(menus, requiredMenus));

    assertEquals(sizeMenuListWithoutSeparators(menus), requiredMenus.size()); // + 1 stands for menu separator
  }

  public static boolean containsAllMenus(Collection<IMenu> reference, Collection<IMenu> menus) {
    // normalize reference
    List<IMenu> refNormalized = new ArrayList<>(reference.size());
    for (IMenu m : reference) {
      refNormalized.add(MenuWrapper.unwrapMenu(m));
    }
    List<IMenu> menusNormalized = new ArrayList<>(menus.size());
    for (IMenu m : menus) {
      menusNormalized.add(MenuWrapper.unwrapMenu(m));
    }
    return refNormalized.containsAll(menusNormalized);
  }

  public static int sizeMenuListWithoutSeparators(Collection<IMenu> menus) {
    int i = 0;
    for (IMenu m : menus) {
      if (!m.isSeparator()) {
        i++;
      }
    }
    return i;
  }

  @SafeVarargs
  private static List<IMenu> resolveMenusOfPageWithTable(IPageWithTable<?> page, Class<? extends IMenu>... menuClasses) {
    List<IMenu> resolvedMenus = new LinkedList<>();

    for (Class<? extends IMenu> menuClass : menuClasses) {
      IMenu menu = page.getTable().getMenuByClass(menuClass);
      assertNotNull(menu);

      resolvedMenus.add(menu);
    }

    return resolvedMenus;
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{new Object[]{"a", "b"}});
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
      return new SubPageWithTable();
    }

    public class Table extends AbstractTable {

      @Order(10)
      public class PageWithTableEmptySpaceMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return "EmptySpace";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.EmptySpace);
        }

      }

      @Order(20)
      public class PageWithTableRowMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return "Edit";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.SingleSelection);
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

  public static class SubPageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{new Object[]{"sub_a", "sub_b"}});
    }

    public class Table extends AbstractTable {

      @Order(10)
      public class SubPageWithTableEmptySpaceMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return "EmptySpace";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.EmptySpace);
        }

      }

      @Order(20)
      public class SubPageWithTableRowMenu extends AbstractMenu {

        @Override
        protected String getConfiguredText() {
          return "Edit";
        }

        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return CollectionUtility.hashSet(TableMenuType.SingleSelection);
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
}
