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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;

/**
 * The filter accepts all checked rows
 */
class CheckedRowsFilter implements ITableRowFilter {

  public CheckedRowsFilter() {
  }

  @Override
  public boolean accept(ITableRow row) {
    if (row.isChecked()) {
      return true;
    }
    return false;
  }

}
