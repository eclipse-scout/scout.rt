/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, TableRow, TableTextUserFilterModel, TableUserFilter, TableUserFilterAddedEventData} from '../../index';

export class TableTextUserFilter extends TableUserFilter implements TableTextUserFilterModel {
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
