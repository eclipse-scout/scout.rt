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
