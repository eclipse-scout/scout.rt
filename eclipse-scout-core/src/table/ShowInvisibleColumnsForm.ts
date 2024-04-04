/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Cell, Column, Form, scout, ShowInvisibleColumnsFormModel, ShowInvisibleColumnsFormWidgetMap, strings, Table, TableRow, WidgetModel} from '../index';
import model from './ShowInvisibleColumnsFormModel';

export class ShowInvisibleColumnsForm extends Form implements ShowInvisibleColumnsFormModel {
  declare model: ShowInvisibleColumnsFormModel;
  declare widgetMap: ShowInvisibleColumnsFormWidgetMap;

  table: Table;
  insertAfterColumn: Column;

  constructor() {
    super();
    this.table = null;
    this.insertAfterColumn = null;
  }

  protected override _jsonModel(): WidgetModel {
    return model();
  }

  override importData() {
    let rows = this._getAddableColumns().map(column => {
      return {
        cells: [
          scout.create(Cell, {value: column}),
          this._getColumnTitle(column)
        ]
      } as TableRow;
    });
    this.widget('ColumnsTable').insertRows(rows);
  }

  protected _getAddableColumns(): Column[] {
    // FIXME bsh [js-table] Find a better solution, maybe list of columns in form data?
    if (this.table.tableOrganizer) {
      return this.table.tableOrganizer.getAddableColumns(this.insertAfterColumn);
    }
    return this.table.displayableColumns()
      .filter(column => !column.visible);
  }

  protected _getColumnTitle(column: Column): string {
    let text = column.text;
    let htmlEnabled = column.headerHtmlEnabled;
    if (strings.empty(text)) {
      text = column.headerTooltipText;
      htmlEnabled = column.headerTooltipHtmlEnabled;
    }
    return htmlEnabled ? strings.plainText(text) : text;
  }

  override exportData(): any {
    let columnsTable = this.widget('ColumnsTable');
    let keyColumn = columnsTable.columnById('KeyColumn');
    let columnsToInsert = columnsTable.checkedRows().map(row => keyColumn.cellValue(row) as unknown as Column<any>); // FIXME bsh [js-table] ask FSH

    // Move (still hidden) columns to a position right after the "insertAfterColumn".
    // We do this _before_ making them visible to prevent the animation.
    if (this.insertAfterColumn && this.insertAfterColumn.visible && this.table.columns.includes(this.insertAfterColumn)) {
      arrays.removeAll(this.table.columns, columnsToInsert);
      arrays.insertAll(this.table.columns, columnsToInsert, this.table.columns.indexOf(this.insertAfterColumn) + 1);
    }

    // Make the columns visible (at their new location)
    columnsToInsert.forEach(column => column.setVisible(true));

    return null;
  }
}
