/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, InitModelOf, LookupRow, scout, SomeRequired, StaticLookupCall, Table, TileTableHeaderGroupByLookupCallModel} from '../index';

export class TileTableHeaderGroupByLookupCall extends StaticLookupCall<Column<any>> implements TileTableHeaderGroupByLookupCallModel {
  declare model: TileTableHeaderGroupByLookupCallModel;
  declare initModel: SomeRequired<this['model'], 'session' | 'table'>;
  table: Table;

  constructor() {
    super();
    this.table = null;
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
  }

  protected override _data(): any[] {
    let lookupRows = [];
    lookupRows.push([null, this.session.text('NoGrouping'), 'BOLD']);
    this.table.visibleColumns().forEach(column => {
      if (this.table.isGroupingPossible(column)) {
        lookupRows.push([column, scout.nvl(column.text, column.headerTooltipText)]);
      }
    });
    return lookupRows;
  }

  protected override _dataToLookupRow(data: any): LookupRow<Column<any>> {
    return scout.create(LookupRow, {
      key: data[0],
      text: data[1],
      font: data[2]
    }) as LookupRow<Column<any>>;
  }
}
