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

import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashSet;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.CompositeSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.CompositeSubMenu.EmptySpaceSubSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.CompositeSubMenu.MultiSelectionSubSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.CompositeSubMenu.SingleSelectionSubSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.EmptySpaceSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.MultiSelectionSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.CompositeMenu.SingleSelectionSubMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.EmptySpaceMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.MultiSelectionMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuTest.Table2.SingleSelectionMenu;
import org.eclipse.scout.rt.client.ui.action.menu.fixture.OwnerValueCapturingMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link TableContextMenu}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableMenuTest {

  private OwnerValueCapturingMenu m_multi;
  private OwnerValueCapturingMenu m_single;
  private OwnerValueCapturingMenu m_emptySpace;
  private OwnerValueCapturingMenu m_combindedSingle;
  private OwnerValueCapturingMenu m_all;

  @Before
  public void before() {
    m_multi = new OwnerValueCapturingMenu(TableMenuType.MultiSelection);
    m_single = new OwnerValueCapturingMenu(TableMenuType.SingleSelection);
    m_emptySpace = new OwnerValueCapturingMenu(TableMenuType.EmptySpace);
    m_combindedSingle = new OwnerValueCapturingMenu(TableMenuType.EmptySpace, TableMenuType.SingleSelection);
    m_all = new OwnerValueCapturingMenu(TableMenuType.values());
  }

  private static final Object[][] TEST_ROWS = new Object[][]{
      {"Boss", "Hugo"},
      {"Meier", "Hans"}
  };

  /**
   * Tests the visibility for a single selection menu
   */
  @Test
  public void testSingleSelection() {
    Table t = new Table();
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();

    // single hugo boss
    t.selectRows(CollectionUtility.arrayList(t.getRow(0)), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertEquals(2, visibleMenus.size());
    assertEquals("SingleSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());
    assertEquals("HugoBossMenu", visibleMenus.get(1).getClass().getSimpleName());

    // single only meier
    t.selectRows(CollectionUtility.arrayList(t.getRow(1)), false);
    filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertEquals(1, visibleMenus.size());
    assertEquals("SingleSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());
  }

  /**
   * Tests the visibility for a multi selection menu
   */
  @Test
  public void setMultiSelection() {
    Table t = new Table();
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();
    // multi selection
    t.selectRows(CollectionUtility.arrayList(t.getRow(0), t.getRow(1)), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertEquals(1, visibleMenus.size());
    assertEquals("MultiSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());
  }

  /**
   * Tests the visibility for a empty space menu
   */
  @Test
  public void testEmptySelection() {
    Table t = new Table();
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();
    // empty selection
    t.selectRows(CollectionUtility.emptyArrayList(), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertEquals(1, visibleMenus.size());
    assertEquals("EmptySpaceMenu", visibleMenus.get(0).getClass().getSimpleName());
  }

  /**
   * Tests empty space menu disabled if table disabled, empty selection
   */
  @Test
  public void testTableDisabledEmptySelection() {
    Table t = new Table();
    t.setEnabled(false);
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();

    t.selectRows(CollectionUtility.emptyArrayList(), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertFalse(visibleMenus.get(0).isEnabledIncludingParents());
  }

  /**
   * Tests menu disabled if table disabled, single selection
   */
  @Test
  public void testTableDisabledSingleSelection() {
    Table t = new Table();
    t.setEnabled(false);
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();

    t.selectRows(CollectionUtility.arrayList(t.getRow(0)), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertFalse(visibleMenus.get(0).isEnabledIncludingParents());
  }

  /**
   * Tests menu empty space menu enabled if row disabled, empty selection
   */
  @Test
  public void testRowDisabledEmptySelection() {
    Table t = new Table();
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();

    t.getRow(0).setEnabled(false);

    t.selectRows(CollectionUtility.emptyArrayList(), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertTrue(visibleMenus.get(0).isEnabled());
  }

  /**
   * Tests menu disabled if row disabled, single selection
   */
  @Test
  public void testRowDisabledSingleSelection() {
    Table t = new Table();
    t.addRowsByMatrix(TEST_ROWS);
    ITableContextMenu contextMenu = t.getContextMenu();

    t.getRow(0).setEnabled(false);

    t.selectRows(CollectionUtility.arrayList(t.getRow(0)), false);
    Predicate<IMenu> filter = MenuUtility.createMenuFilterMenuTypes(contextMenu.getCurrentMenuTypes(), true);
    List<IMenu> visibleMenus = MenuUtility.normalizedMenus(contextMenu.getChildActions(), filter);
    assertFalse(visibleMenus.get(0).isEnabledIncludingParents());
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called only multi-selection menus, if multiple rows
   * are selected.
   */
  @Test
  public void testOwnerValueOnMultiSelection() {
    final ContextMenuTable table = createContextMenuTable();
    addTestMenus(table);

    table.selectAllRows();

    assertOwnerValueChange(m_multi, 2);
    assertOwnerValueChange(m_all, 2);
    assertNoOwnerValueChange(m_single);
    assertNoOwnerValueChange(m_emptySpace);
    assertNoOwnerValueChange(m_combindedSingle);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called only single-selection menus, if the a single
   * row is selected.
   */
  @Test
  public void testOwnerValueOnSingleSelection() {
    final ContextMenuTable table = createContextMenuTable();
    addTestMenus(table);
    table.selectFirstRow();

    assertOwnerValueChange(m_single, 1);
    assertOwnerValueChange(m_all, 1);
    assertNoOwnerValueChange(m_multi);
    assertNoOwnerValueChange(m_emptySpace);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called for empty space menus, if empty space is
   * selected.
   */
  @Test
  public void testOwnerValueOnEmptySpace() {
    final ContextMenuTable table = createContextMenuTable();
    table.selectAllRows();
    addTestMenus(table);
    table.deselectAllRows();

    assertOwnerValueChange(m_emptySpace, 0);
    assertOwnerValueChange(m_all, 0);
    assertNoOwnerValueChange(m_multi);
    assertNoOwnerValueChange(m_single);
  }

  @Test
  public void testMenuEnabledStateDisabledRow() {
    Table2 table = new Table2();
    table.addRowsByMatrix(TEST_ROWS);
    table.getRow(0).setEnabled(false);

    SingleSelectionMenu singleSelectionMenu = table.getMenuByClass(SingleSelectionMenu.class);
    MultiSelectionMenu multiSelectionMenu = table.getMenuByClass(MultiSelectionMenu.class);
    EmptySpaceMenu emptySpaceMenu = table.getMenuByClass(EmptySpaceMenu.class);
    CompositeMenu compositeMenu = table.getMenuByClass(CompositeMenu.class);
    SingleSelectionSubMenu singleSelectionSubMenu = table.getMenuByClass(SingleSelectionSubMenu.class);
    MultiSelectionSubMenu multiSelectionSubMenu = table.getMenuByClass(MultiSelectionSubMenu.class);
    EmptySpaceSubMenu emptySpaceSubMenu = table.getMenuByClass(EmptySpaceSubMenu.class);
    CompositeSubMenu compositeSubMenu = table.getMenuByClass(CompositeSubMenu.class); // has inheritAccessibility=false
    SingleSelectionSubSubMenu selectionSubSubMenu = table.getMenuByClass(SingleSelectionSubSubMenu.class);
    MultiSelectionSubSubMenu multiSelectionSubSubMenu = table.getMenuByClass(MultiSelectionSubSubMenu.class);
    EmptySpaceSubSubMenu emptySpaceSubSubMenu = table.getMenuByClass(EmptySpaceSubSubMenu.class);

    assertTrue(singleSelectionMenu.isEnabled());
    assertTrue(multiSelectionMenu.isEnabled());
    assertTrue(emptySpaceMenu.isEnabled());
    assertTrue(compositeMenu.isEnabled());
    assertTrue(singleSelectionSubMenu.isEnabled());
    assertTrue(multiSelectionSubMenu.isEnabled());
    assertTrue(emptySpaceSubMenu.isEnabled());
    assertTrue(compositeSubMenu.isEnabled());
    assertTrue(selectionSubSubMenu.isEnabled());
    assertTrue(multiSelectionSubSubMenu.isEnabled());
    assertTrue(emptySpaceSubSubMenu.isEnabled());

    table.selectFirstRow();
    assertFalse(singleSelectionMenu.isEnabled());
    assertFalse(multiSelectionMenu.isEnabled());
    assertTrue(emptySpaceMenu.isEnabled()); // stays enabled because it is an empty-space menu and therefore not selection dependent
    assertTrue(compositeMenu.isEnabled()); // stays enabled because there are enabled child menus
    assertFalse(singleSelectionSubMenu.isEnabled());
    assertFalse(multiSelectionSubMenu.isEnabled());
    assertTrue(emptySpaceSubMenu.isEnabled()); // stays enabled because it is an empty-space menu and therefore not selection dependent
    assertTrue(compositeSubMenu.isEnabled());
    assertTrue(selectionSubSubMenu.isEnabled());
    assertTrue(multiSelectionSubSubMenu.isEnabled());
    assertTrue(emptySpaceSubSubMenu.isEnabled());

    table.setEnabled(false);
    assertFalse(table.getContextMenu().isEnabled());
  }

  /// HELPERS

  private void addTestMenus(ITable table) {
    table.addMenu(m_emptySpace);
    table.addMenu(m_single);
    table.addMenu(m_multi);
    table.addMenu(m_all);
    table.addMenu(m_combindedSingle);
  }

  private ContextMenuTable createContextMenuTable() {
    final ContextMenuTable table = new ContextMenuTable();
    table.addRowsByMatrix(TEST_ROWS);
    return table;
  }

  private void assertOwnerValueChange(OwnerValueCapturingMenu menu, int rows) {
    assertEquals(1, menu.getCount());
    assertTrue("Owner should be a collection of 2 rows " + menu.getLastOwnerValue().getClass(), menu.getLastOwnerValue() instanceof Collection);
    assertEquals(rows, ((Collection) menu.getLastOwnerValue()).size());
  }

  private void assertNoOwnerValueChange(OwnerValueCapturingMenu menu) {
    assertEquals(0, menu.getCount());
  }

  /// FIXTURES

  public class Table extends AbstractTable {
    public NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    public PrenameColumn getPrenameColumn() {
      return getColumnSet().getColumnByClass(PrenameColumn.class);
    }

    @Order(10)
    public class NameColumn extends AbstractStringColumn {

    }

    @Order(10)
    public class PrenameColumn extends AbstractStringColumn {

    }

    @Order(100)
    public class SingleSelectionMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.SingleSelection);
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) {
        setEnabled(true);
      }
    }

    @Order(111)
    public class HugoBossMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.SingleSelection);
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) {
        setEnabled(true);
        setVisible(ObjectUtility.equals(getPrenameColumn().getSelectedValue(), "Hugo"));
      }
    }

    @Order(110)
    public class MultiSelectionMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.MultiSelection);
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) {
        setEnabled(true);
      }
    }

    @Order(120)
    public class EmptySpaceMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.EmptySpace);
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) {
        setEnabled(true);
      }
    }
  }

  private static class ContextMenuTable extends AbstractTable {
    @Override
    public void setContextMenu(ITableContextMenu contextMenu) {
      super.setContextMenu(contextMenu);
    }
  }

  public static class Table2 extends AbstractTable {
    @Order(10)
    public class LastNameColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class FirstNameColumn extends AbstractStringColumn {
    }

    @Order(10)
    public class SingleSelectionMenu extends AbstractMenu {
      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.SingleSelection);
      }
    }

    @Order(20)
    public class MultiSelectionMenu extends AbstractMenu {
      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.MultiSelection);
      }
    }

    @Order(25)
    public class EmptySpaceMenu extends AbstractMenu {
      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return hashSet(TableMenuType.EmptySpace);
      }
    }

    @Order(30)
    public class Separator1Menu extends AbstractMenu {
      @Override
      protected boolean getConfiguredSeparator() {
        return true;
      }
    }

    @Order(40)
    public class CompositeMenu extends AbstractMenu {
      @Order(10)
      public class SingleSelectionSubMenu extends AbstractMenu {
        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return hashSet(TableMenuType.SingleSelection);
        }
      }

      @Order(20)
      public class MultiSelectionSubMenu extends AbstractMenu {
        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return hashSet(TableMenuType.MultiSelection);
        }
      }

      @Order(25)
      public class EmptySpaceSubMenu extends AbstractMenu {
        @Override
        protected Set<? extends IMenuType> getConfiguredMenuTypes() {
          return hashSet(TableMenuType.EmptySpace);
        }
      }

      @Order(30)
      public class Separator2Menu extends AbstractMenu {
        @Override
        protected boolean getConfiguredSeparator() {
          return true;
        }
      }

      @Order(40)
      public class CompositeSubMenu extends AbstractMenu {
        @Override
        protected boolean getConfiguredInheritAccessibility() {
          return false;
        }

        @Order(10)
        public class SingleSelectionSubSubMenu extends AbstractMenu {
          @Override
          protected Set<? extends IMenuType> getConfiguredMenuTypes() {
            return hashSet(TableMenuType.SingleSelection);
          }
        }

        @Order(20)
        public class Separator3Menu extends AbstractMenu {
          @Override
          protected boolean getConfiguredSeparator() {
            return true;
          }
        }

        @Order(25)
        public class EmptySpaceSubSubMenu extends AbstractMenu {
          @Override
          protected Set<? extends IMenuType> getConfiguredMenuTypes() {
            return hashSet(TableMenuType.EmptySpace);
          }
        }

        @Order(30)
        public class MultiSelectionSubSubMenu extends AbstractMenu {
          @Override
          protected Set<? extends IMenuType> getConfiguredMenuTypes() {
            return hashSet(TableMenuType.MultiSelection);
          }
        }
      }
    }
  }
}
