/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, Column, strings, TableRow} from '../../index';
import $ from 'jquery';

export class BeanColumn<TBean> extends Column<TBean> {

  override buildCellForRow(row: TableRow): string {
    let cell = this.cell(row);
    let cssClass = this._cellCssClass(cell);
    let style = this._cellStyle(cell);
    let $cell = $(super._buildCell(cell, '', style, cssClass));
    let value = this.table.cellValue(this, row) as TBean;

    if (cell.errorStatus) {
      row.hasError = true;
    }

    this._renderValue($cell, value);
    return $cell[0].outerHTML;
  }

  /**
   * Override to render the value.
   * If you have a large table you should consider overriding buildCellForRow instead and create the html as string instead of using jquery.
   */
  protected _renderValue($cell: JQuery, value: TBean) {
    // to be implemented by the subclass
  }

  protected _plainTextForRow(row: TableRow): string {
    let cell = this.table.cell(this, row) as Cell<TBean> & { plainText?: string };
    if (!cell.plainText) {
      // Convert to plain text and cache it because rendering is expensive
      let html = this.buildCellForRow(row);
      cell.plainText = strings.plainText(html);
    }
    return cell.plainText;
  }

  /**
   * Default approach reads the html using buildCellForRow and uses _preprocessTextForGrouping to generate the value. Just using text() does not work because new lines get omitted.
   * If this approach does not work for a specific bean column, just override this method.
   */
  override cellValueOrTextForCalculation(row: TableRow): string {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForCalculation(plainText);
  }

  override cellTextForGrouping(row: TableRow): string {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForGrouping(plainText);
  }

  override cellTextForTextFilter(row: TableRow): string {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForTextFilter(plainText);
  }

  override compare(row1: TableRow, row2: TableRow): number {
    let plainText1 = this._plainTextForRow(row1);
    let plainText2 = this._plainTextForRow(row2);
    return this.comparator.compareIgnoreCase(plainText1, plainText2);
  }
}
