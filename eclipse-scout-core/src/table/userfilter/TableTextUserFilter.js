/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableUserFilter} from '../../index';

export default class TableTextUserFilter extends TableUserFilter {

  constructor() {
    super();
    this.filterType = TableTextUserFilter.TYPE;
  }

  static TYPE = 'text';

  /**
   * @override TableUserFilter.js
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.text = this.text;
    return data;
  }

  createLabel() {
    return this.text;
  }

  accept(row) {
    let rowText = this.table.visibleColumns().reduce((acc, column) => {
      return acc + column.cellTextForTextFilter(row) + ' ';
    }, '');
    let text = scout.nvl(this.text, '');
    if (text !== this._cachedText) {
      this._cachedText = text;
      this._cachedTextLowerCase = text.toLowerCase();
    }
    rowText = rowText.trim().toLowerCase();
    return rowText.indexOf(this._cachedTextLowerCase) > -1;
  }
}
