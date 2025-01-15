/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

/**
 * A table filter is used to mask out certain rows from a table. All rows that are <i>not</i> accept by a filter, are
 * masked out. The remaining rows (i.e. rows that are accepted by all filters) can be retrieved by
 * {@link ITable#getFilteredRows()}. The rows themselves are still in the table! The methods {@link ITable#getRows()}
 * and {@link ITable#getRowCount()} are unaffected by the filter. The UI should use {@link ITable#getFilteredRows()}
 */
@FunctionalInterface
public interface ITableRowFilter {

  /**
   * @return <code>true</code> if the given row is accepted by the filter and should therefore remain in the table's
   * "filtered rows" set. Usually, the UI hides unaccepted rows.
   */
  boolean accept(ITableRow row);
}
