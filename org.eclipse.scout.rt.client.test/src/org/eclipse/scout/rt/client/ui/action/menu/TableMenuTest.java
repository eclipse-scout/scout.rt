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

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(ScoutClientTestRunner.class)
public class TableMenuTest {

  @Test
  public void testSingleSelection() throws Exception {
    Table t = new Table();
    t.addRowsByMatrix(new Object[][]{
        {"Boss", "Hugo"},
        {"Meier", "Hans"}
    });
    ITableContextMenu contextMenu = t.getContextMenu();

    // single hugo boss
    t.selectRows(CollectionUtility.arrayList(t.getRow(0)), false);
    IActionFilter filter = ActionUtility.createCombinedFilter(contextMenu.getActiveFilter(), ActionUtility.createVisibleFilter());
    List<IMenu> visibleMenus = ActionUtility.visibleNormalizedActions(contextMenu.getChildActions(), filter);
    Assert.assertEquals(2, visibleMenus.size());
    Assert.assertEquals("SingleSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());
    Assert.assertEquals("HugoBossMenu", visibleMenus.get(1).getClass().getSimpleName());

    // single only meier
    t.selectRows(CollectionUtility.arrayList(t.getRow(1)), false);
    filter = ActionUtility.createCombinedFilter(contextMenu.getActiveFilter(), ActionUtility.createVisibleFilter());
    visibleMenus = ActionUtility.visibleNormalizedActions(contextMenu.getChildActions(), filter);
    Assert.assertEquals(1, visibleMenus.size());
    Assert.assertEquals("SingleSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());

  }

  @Test
  public void setMultiSeleciton() throws ProcessingException {
    Table t = new Table();
    t.addRowsByMatrix(new Object[][]{
        {"Boss", "Hugo"},
        {"Meier", "Hans"}
    });
    ITableContextMenu contextMenu = t.getContextMenu();
    // multi selection
    t.selectRows(CollectionUtility.arrayList(t.getRow(0), t.getRow(1)), false);
    IActionFilter filter = ActionUtility.createCombinedFilter(contextMenu.getActiveFilter(), ActionUtility.createVisibleFilter());
    List<IMenu> visibleMenus = ActionUtility.visibleNormalizedActions(contextMenu.getChildActions(), filter);
    Assert.assertEquals(1, visibleMenus.size());
    Assert.assertEquals("MultiSelectionMenu", visibleMenus.get(0).getClass().getSimpleName());

  }

  @Test
  public void testEmptySeleciton() throws ProcessingException {
    Table t = new Table();
    t.addRowsByMatrix(new Object[][]{
        {"Boss", "Hugo"},
        {"Meier", "Hans"}
    });
    ITableContextMenu contextMenu = t.getContextMenu();
    // empty selection
    t.selectRows(CollectionUtility.<ITableRow> emptyArrayList(), false);
    IActionFilter filter = ActionUtility.createCombinedFilter(contextMenu.getActiveFilter(), ActionUtility.createVisibleFilter());
    List<IMenu> visibleMenus = ActionUtility.visibleNormalizedActions(contextMenu.getChildActions(), filter);
    Assert.assertEquals(1, visibleMenus.size());
    Assert.assertEquals("EmptySpaceMenu", visibleMenus.get(0).getClass().getSimpleName());

  }

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
        return CollectionUtility.hashSet(TableMenuType.SingleSelection);
      }

    }

    @Order(111)
    public class HugoBossMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TableMenuType.SingleSelection);
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
        setVisible(CompareUtility.equals(getPrenameColumn().getSelectedValue(), "Hugo"));
      }
    }

    @Order(110)
    public class MultiSelectionMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TableMenuType.MultiSelection);
      }

    }

    @Order(120)
    public class EmptySpaceMenu extends AbstractMenu {

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TableMenuType.EmptySpace);
      }
    }
  }
}
