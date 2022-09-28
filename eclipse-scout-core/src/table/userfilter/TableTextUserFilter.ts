/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, TableRow, TableTextUserFilterModel, TableUserFilter} from '../../index';
import {TableUserFilterAddedEventData} from './TableUserFilter';

export default class TableTextUserFilter extends TableUserFilter implements TableTextUserFilterModel {
  declare model: TableTextUserFilterModel;

  text: string;

  protected _cachedText: string;
  protected _cachedTextLowerCase: string;

  constructor() {
    super();
    this.filterType = TableTextUserFilter.TYPE;
  }

  static TYPE = 'text';

  override createFilterAddedEventData(): TableUserFilterAddedEventData {
    let data = super.createFilterAddedEventData();
    data.text = this.text;
    return data;
  }

  createLabel(): string {
    return this.text;
  }

  accept(row: TableRow): boolean {
    let rowText = this.table.visibleColumns().reduce((acc, column) => acc + column.cellTextForTextFilter(row) + ' ', '');
    let text = scout.nvl(this.text, '');
    if (text !== this._cachedText) {
      this._cachedText = text;
      this._cachedTextLowerCase = text.toLowerCase();
    }
    rowText = rowText.trim().toLowerCase();
    return rowText.indexOf(this._cachedTextLowerCase) > -1;
  }
}
