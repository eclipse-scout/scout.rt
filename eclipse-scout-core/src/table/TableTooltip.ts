/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, InitModelOf, SomeRequired, Table, TableRow, TableRowOrderChangedEvent, TableTooltipModel, Tooltip} from '../index';

export class TableTooltip extends Tooltip implements TableTooltipModel {
  declare model: TableTooltipModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'table'>;

  table: Table;
  row: TableRow;

  protected _rowOrderChangedFunc: EventHandler<TableRowOrderChangedEvent>;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);

    this.table = options.table;
  }

  protected override _render() {
    super._render();

    this._rowOrderChangedFunc = (event: TableRowOrderChangedEvent) => {
      if (event.animating) {
        // row is only set while animating
        if (event.row === this.row) {
          this.position();
        }
      } else {
        this.position();
      }
    };
    this.table.on('rowOrderChanged', this._rowOrderChangedFunc);
  }

  protected override _remove() {
    super._remove();
    this.table.off('rowOrderChanged', this._rowOrderChangedFunc);
  }
}
