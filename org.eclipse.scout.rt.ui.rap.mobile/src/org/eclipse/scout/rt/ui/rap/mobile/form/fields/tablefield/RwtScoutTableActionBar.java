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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class RwtScoutTableActionBar extends AbstractRwtScoutActionBar<ITableField<? extends ITable>> {
  private static final String VARIANT_ACTION_BAR_CONTAINER = "mobileTableActionBarContainer";

  private P_TableRowSelectionListener m_rowSelectionListener;
  private ITable m_table;

  public RwtScoutTableActionBar() {
    setMenuOpeningDirection(SWT.UP);
  }

  @Override
  protected void initLayout(Composite container) {
    super.initLayout(container);

    int tableStatusGridH = 1;
    LogicalGridData tableGridData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    LogicalGridData gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh + tableStatusGridH;
    gd.gridw = tableGridData.gridw;
    gd.topInset = 0;
    gd.gridh = 1;
    if (getHeightHint() != null) {
      gd.heightHint = getHeightHint();
    }
    else {
      gd.useUiHeight = true;
    }
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    container.setLayoutData(gd);
  }

  @Override
  protected void collectMenusForLeftButtonBar(List<IMenu> menuList) {
    ITable table = getScoutObject().getTable();
    if (table == null) {
      return;
    }

    IMenu[] emptySpaceMenus = RwtMenuUtility.collectEmptySpaceMenus(table, getUiEnvironment());
    if (emptySpaceMenus != null) {
      menuList.addAll(Arrays.asList(emptySpaceMenus));
    }

    IMenu[] rowMenus = RwtMenuUtility.collectRowMenus(table, getUiEnvironment());
    if (rowMenus != null) {
      List<IMenu> rowMenuList = new LinkedList<IMenu>(Arrays.asList(rowMenus));

      ActionButtonBarUtility.distributeRowActions(menuList, emptySpaceMenus, rowMenuList);

      //Add remaining row menus
      menuList.addAll(rowMenuList);
    }
  }

  @Override
  protected void collectMenusForRightButtonBar(List<IMenu> menuList) {

  }

  @Override
  protected String getActionBarContainerVariant() {
    return VARIANT_ACTION_BAR_CONTAINER;
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    m_table = getScoutObject().getTable();

    addRowSelectionListener(m_table);
  }

  @Override
  protected void detachScout() {
    super.detachScout();

    removeRowSelectionListener(m_table);

    m_table = null;
  }

  private void addRowSelectionListener(ITable table) {
    if (m_rowSelectionListener != null || table == null) {
      return;
    }

    m_rowSelectionListener = new P_TableRowSelectionListener();
    table.addTableListener(m_rowSelectionListener);
  }

  private void removeRowSelectionListener(ITable table) {
    if (m_rowSelectionListener == null || table == null) {
      return;
    }

    table.removeTableListener(m_rowSelectionListener);
    m_rowSelectionListener = null;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);

    if (name.equals(ITableField.PROP_TABLE)) {
      removeRowSelectionListener(m_table);

      m_table = (ITable) newValue;

      addRowSelectionListener(m_table);
    }
  }

  private class P_TableRowSelectionListener extends TableAdapter {

    @Override
    public void tableChanged(TableEvent e) {
      if (e.getType() == TableEvent.TYPE_ROWS_SELECTED) {
        rowSelected();
      }
    }

    private void rowSelected() {
      rebuildContentFromScout();
    }

  }

}
