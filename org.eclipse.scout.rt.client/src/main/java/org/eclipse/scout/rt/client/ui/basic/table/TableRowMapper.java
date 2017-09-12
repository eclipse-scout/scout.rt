/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.internal.HolderToRowMapper;
import org.eclipse.scout.rt.client.ui.basic.table.internal.RowToHolderMapper;
import org.eclipse.scout.rt.platform.holders.IHolder;

public class TableRowMapper {
  private final ITableRow m_row;
  private List<RowToHolderMapper> m_exportMappings;
  private List<HolderToRowMapper> m_importMappings;

  public TableRowMapper(ITableRow row) {
    m_row = row;
  }

  public ITableRow getRow() {
    return m_row;
  }

  /**
   * short form for addMapping(col,holder,true,true) {@link #addMapping(IColumn, IHolder, boolean, boolean)}
   */
  public <T> void addMapping(IColumn<T> col, IHolder<T> holder) {
    addMapping(col, holder, true, true);
  }

  /**
   * Convenience for mapping the values of this row to holders such as form fields and value containers
   *
   * @see #exportRowData() and @see {@link #importRowData()}
   * @param enableExport
   *          if true then the corresponding row value is included in {@link #exportRowData()}
   * @param enableImport
   *          if true then the corresponding row value is included in {@link #importRowData()}
   */
  public <T> void addMapping(IColumn<T> col, IHolder<T> holder, boolean enableExport, boolean enableImport) {
    if (enableExport) {
      if (m_exportMappings == null) {
        m_exportMappings = new ArrayList<>();
      }
      m_exportMappings.add(new RowToHolderMapper<>(m_row, col, holder));
    }
    if (enableImport) {
      if (m_importMappings == null) {
        m_importMappings = new ArrayList<>();
      }
      m_importMappings.add(new HolderToRowMapper<>(m_row, col, holder));
    }
  }

  public void exportRowData() {
    if (m_exportMappings != null) {
      try {
        m_row.setRowChanging(true);
        //
        for (RowToHolderMapper m : m_exportMappings) {
          m.exportRowValue();
        }
      }
      finally {
        m_row.setRowChanging(false);
      }
    }
  }

  public void importRowData() {
    if (m_importMappings != null) {
      try {
        m_row.setRowChanging(true);
        //
        for (HolderToRowMapper m : m_importMappings) {
          m.importRowValue();
        }
      }
      finally {
        m_row.setRowChanging(false);
      }
    }
  }

}
