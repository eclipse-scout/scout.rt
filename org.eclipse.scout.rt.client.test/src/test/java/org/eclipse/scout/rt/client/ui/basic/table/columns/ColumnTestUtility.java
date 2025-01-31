/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
