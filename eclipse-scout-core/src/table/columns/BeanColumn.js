/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column, strings} from '../../index';
import $ from 'jquery';

export default class BeanColumn extends Column {

  constructor() {
    super();
  }

  buildCellForRow(row) {
    let cell = this.cell(row);
    let cssClass = this._cellCssClass(cell);
    let style = this._cellStyle(cell);
    let $cell = $(super._buildCell(cell, '', style, cssClass));
    let value = this.table.cellValue(this, row);

    if (cell.errorStatus) {
      row.hasError = true;
    }

    this._renderValue($cell, value);
    return $cell[0].outerHTML;
  }

  /**
   * Override to render the value.<p>
   * If you have a large table you should consider overriding buildCellForRow instead and create the html as string instead of using jquery.
   */
  _renderValue($cell, value) {
    // to be implemented by the subclass
  }

  _plainTextForRow(row) {
    let cell = this.table.cell(this, row);
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
  cellValueOrTextForCalculation(row) {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForCalculation(plainText);
  }

  cellTextForGrouping(row) {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForGrouping(plainText);
  }

  cellTextForTextFilter(row) {
    let plainText = this._plainTextForRow(row);
    return this._preprocessTextForTextFilter(plainText);
  }

  compare(row1, row2) {
    let plainText1 = this._plainTextForRow(row1);
    let plainText2 = this._plainTextForRow(row2);
    return this.comparator.compareIgnoreCase(plainText1, plainText2);
  }
}
