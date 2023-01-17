/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormWithTableField.MainBox.TableInOrigFormField;

/**
 * @author Matthias Villiger
 */
@FormData(value = OrigFormWithTableFieldData.class, sdkCommand = SdkCommand.CREATE)
public class OrigFormWithTableField extends AbstractForm {

  public static final String FIRST_ROW_VALUE = "first row";

  public TableInOrigFormField getTableInOrigFormField() {
    return getFieldByClass(TableInOrigFormField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  @Order(1000)
  public class MainBox extends AbstractGroupBox {

    @Order(1000.0)
    public class TableInOrigFormField extends AbstractTableField<TableInOrigFormField.Table> {
      public class Table extends AbstractTable {

        @Override
        protected void execInitTable() {
          ITableRow firstRow = addRow();
          getOrigColumn().setValue(firstRow, FIRST_ROW_VALUE);
        }

        public OrigColumn getOrigColumn() {
          return getColumnSet().getColumnByClass(OrigColumn.class);
        }

        @Order(1000.0)
        public class OrigColumn extends AbstractStringColumn {
        }
      }
    }
  }
}
