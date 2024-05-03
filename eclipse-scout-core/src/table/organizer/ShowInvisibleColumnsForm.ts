/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Cell, Column, Form, FormModel, scout, ShowInvisibleColumnsFormData, ShowInvisibleColumnsFormModel, ShowInvisibleColumnsFormWidgetMap, strings, TableRow} from '../../index';
import model from './ShowInvisibleColumnsFormModel';

export class ShowInvisibleColumnsForm extends Form implements ShowInvisibleColumnsFormModel {
  declare model: ShowInvisibleColumnsFormModel;
  declare widgetMap: ShowInvisibleColumnsFormWidgetMap;
  declare data: ShowInvisibleColumnsFormData;

  protected override _jsonModel(): FormModel {
    return model();
  }

  override importData() {
    let invisibleColumns = arrays.ensure(this.data?.columns);
    let rows = invisibleColumns.map(column => {
      return {
        cells: [
          column,
          false,
          this._createTitleCell(column)
        ]
      } as TableRow;
    });
    this.widget('ColumnsTable').insertRows(rows);
  }

  override exportData(): ShowInvisibleColumnsFormData {
    let columnsTable = this.widget('ColumnsTable');
    let keyColumn = columnsTable.columnById('KeyColumn');
    let selectedColumns = columnsTable.checkedRows().map(row => keyColumn.cellValue(row));

    return {
      columns: selectedColumns
    };
  }

  protected _createTitleCell(column: Column): Cell {
    let text = column.text;
    let htmlEnabled = column.headerHtmlEnabled;
    let font = null;
    if (strings.empty(text)) {
      text = column.headerTooltipText;
      htmlEnabled = column.headerTooltipHtmlEnabled;
      font = 'italic';
    }

    let title = htmlEnabled ? strings.plainText(text) : text;
    return scout.create(Cell, {
      value: title,
      font: font
    });
  }
}
