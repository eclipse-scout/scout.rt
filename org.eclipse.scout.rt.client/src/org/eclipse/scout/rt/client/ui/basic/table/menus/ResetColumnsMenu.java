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

  @Order(0)
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
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(5)
  public class Separator1Menu extends MenuSeparator {
  }

  @Order(10)
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

  @Order(20)
  public class ResetWidthsMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsWidth");
    }

    @Override
    protected void execAction() {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetColumnWidths();
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(30)
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

  @Order(40)
  public class ResetOrderMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsOrder");
    }

    @Override
    protected void execAction() {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetColumnOrder();
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Order(50)
  public class ResetVisibilityMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("ResetTableColumnsVisibility");
    }

    @Override
    protected void execAction() {
      try {
        m_table.setTableChanging(true);
        //
        m_table.resetColumnVisibilities();
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

}
