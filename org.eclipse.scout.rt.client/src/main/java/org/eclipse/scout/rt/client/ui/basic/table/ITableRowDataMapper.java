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

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * Maps table rows from an {@link ITable} to an {@link AbstractTableRowData} and vice versa.
 *
 * @since 3.10.0-M5
 */
public interface ITableRowDataMapper {

  /**
   * Writes the data from the given {@link AbstractTableRowData} to the given {@link ITableRow}.
   *
   * @param row
   * @param rowData
   */
  void importTableRowData(ITableRow row, AbstractTableRowData rowData);

  /**
   * Writes the data from the given {@link ITableRow} to the given {@link AbstractTableRowData}.
   *
   * @param row
   * @param rowData
   */
  void exportTableRowData(ITableRow row, AbstractTableRowData rowData);

  /**
   * Override to exclude certain rows from being exported.
   * <p>
   * As default every row is accepted.
   */
  boolean acceptExport(ITableRow row);

  /**
   * Override to exclude certain rows from being imported.
   * <p>
   * As default every row is accepted.
   */
  boolean acceptImport(AbstractTableRowData rowData);
}
