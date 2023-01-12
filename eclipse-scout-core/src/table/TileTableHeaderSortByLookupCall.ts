/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, icons, LookupRow, scout, SomeRequired, StaticLookupCall, Table, TileTableHeaderSortByLookupCallModel} from '../index';

export class TileTableHeaderSortByLookupCall extends StaticLookupCall<TileTableHeaderSortKey> implements TileTableHeaderSortByLookupCallModel {
  declare model: TileTableHeaderSortByLookupCallModel;
  declare initModel: SomeRequired<this['model'], 'session' | 'table'>;
  table: Table;

  constructor() {
    super();
    this.table = null;
  }

  protected override _data(): any[] {
    let lookupRows = [];
    this.table.visibleColumns().forEach(column => {
      if (column.isSortingPossible()) {
        lookupRows.push([
          {
            column: column,
            asc: true
          },
          scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.ascending') + ')',
          icons.LONG_ARROW_UP_BOLD
        ]);
        lookupRows.push([
          {
            column: column,
            asc: false
          },
          scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.descending') + ')',
          icons.LONG_ARROW_DOWN_BOLD
        ]);
      }
    });
    return lookupRows;
  }

  protected override _dataToLookupRow(data: any): LookupRow<TileTableHeaderSortKey> {
    return scout.create(LookupRow, {
      key: data[0],
      text: data[1],
      iconId: data[2]
    }) as LookupRow<TileTableHeaderSortKey>;
  }
}

export type TileTableHeaderSortKey = { column: Column<any>; asc: boolean };
