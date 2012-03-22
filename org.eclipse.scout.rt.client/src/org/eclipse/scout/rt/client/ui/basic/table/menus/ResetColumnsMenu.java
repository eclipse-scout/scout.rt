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
package org.eclipse.scout.rt.client.ui.basic.table.menus;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class ResetColumnsMenu extends AbstractMenu {
  private final ITable m_table;

  public ResetColumnsMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("ResetTableColumns");
  }

  @Order(10.0)
  public class ResetAllMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsAll");
    }

    @Override
    protected void execAction() throws ProcessingException {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetDisplayableColumns();
        ITableColumnFilterManager m = m_table.getColumnFilterManager();
        if (m != null) {
          m.reset();
        }
        ITableCustomizer cst = m_table.getTableCustomizer();
        if (cst != null) {
          cst.removeAllColumns();
        }
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(20.0)
  public class Separator1Menu extends MenuSeparator {
  }

  @Order(30.0)
  public class ResetViewMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsView");
    }

    @Override
    protected void execAction() throws ProcessingException {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetColumnVisibilities();
        m_table.resetColumnWidths();
        m_table.resetColumnOrder();
        ITableCustomizer cst = m_table.getTableCustomizer();
        if (cst != null) {
          cst.removeAllColumns();
        }
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(40.0)
  public class ResetSortingMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsSorting");
    }

    @Override
    protected void execAction() {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetColumnSortOrder();
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(50.0)
  public class ResetColumnFiltersMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnFilter");
    }

    @Override
    protected void execPrepareAction() throws ProcessingException {
      setVisible(m_table.getColumnFilterManager() != null);
    }

    @Override
    protected void execAction() throws ProcessingException {
      try {
        m_table.setTableChanging(true);
        //
        ITableColumnFilterManager m = m_table.getColumnFilterManager();
        if (m != null) {
          m.reset();
        }
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

}
