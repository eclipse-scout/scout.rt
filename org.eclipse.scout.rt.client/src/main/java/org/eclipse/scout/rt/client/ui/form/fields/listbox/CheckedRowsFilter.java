/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;

/**
 * The filter accepts all checked rows
 */
class CheckedRowsFilter implements ITableRowFilter {

  @Override
  public boolean accept(ITableRow row) {
    if (row.isChecked()) {
      return true;
    }
    return false;
  }
}
