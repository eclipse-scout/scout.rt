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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * Some utility methods for column tests
 */
public final class ColumnTestUtility {

  private ColumnTestUtility() {
  }

  @SuppressWarnings("unchecked")
  public static <T> void editCellValue(ITableRow row, IColumn<T> column, String value) {
    IValueField<T> field = (IValueField<T>) column.prepareEdit(row);
    field.parseAndSetValue(value);
    column.completeEdit(row, field);
  }

}
