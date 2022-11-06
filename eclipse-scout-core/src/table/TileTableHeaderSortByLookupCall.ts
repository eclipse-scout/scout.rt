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
import {Column, icons, LookupRow, scout, StaticLookupCall, Table, TileTableHeaderSortByLookupCallModel} from '../index';
import {SomeRequired} from '../types';

export default class TileTableHeaderSortByLookupCall extends StaticLookupCall<TileTableHeaderSortKey> implements TileTableHeaderSortByLookupCallModel {
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
