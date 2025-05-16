/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, arrays, Event, EventEmitter, EventHandler, Menu, scout, Table, TableRow, TableRowMoveDirection} from '../index';

/**
 * When installed, the helper implements the actions for the given row menus to move the selected rows up or down.
 * It also updates the enabled and visible states of the menus accordingly.
 */
export class MoveTableRowMenuHelper extends EventEmitter {
  table: Table;
  moveRowUpMenu: Menu;
  moveRowDownMenu: Menu;
  selectedRowsFilter: (selectedRow: TableRow, direction: TableRowMoveDirection) => boolean;
  protected _actionHandler: EventHandler;
  protected _tableHandler: EventHandler;

  install(options: MoveRowMenuInstallOptions) {
    if (this.table) {
      // Already installed
      return;
    }
    this.table = scout.assertInstance(options.table, Table);
    this.moveRowUpMenu = scout.assertInstance(options.moveRowUpMenu, Menu);
    this.moveRowDownMenu = scout.assertInstance(options.moveRowDownMenu, Menu);
    this.selectedRowsFilter = options.selectedRowsFilter;

    this._actionHandler = this._onMoveRowMenuAction.bind(this);
    this._tableHandler = this._onTableEvent.bind(this);

    this.moveRowUpMenu.on('action', this._actionHandler);
    this.moveRowDownMenu.on('action', this._actionHandler);
    this.table.on('rowsSelected rowsInserted rowsUpdated rowsDeleted rowOrderChanged', this._tableHandler);
    this._updateMenus();
  }

  uninstall() {
    if (!this.table) {
      // Not installed yet
      return;
    }
    this.moveRowUpMenu.off('action', this._actionHandler);
    this.moveRowDownMenu.off('action', this._actionHandler);
    this.table.off('rowsSelected rowsInserted rowsUpdated rowsDeleted rowOrderChanged', this._tableHandler);
  }

  protected _updateMenus() {
    let moveable = this.table.selectedRows.length && this.table.rows.length > 1;
    let firstRow = arrays.first(this.table.visibleRows);
    let lastRow = arrays.last(this.table.visibleRows);
    // Using a new dimension makes it possible to still hide and disable the menus by the consumer of this helper, even if this helper would show or enable the menus
    this.moveRowUpMenu.setPropertyDimension('visible', 'helper', moveable);
    this.moveRowUpMenu.setPropertyDimension('enabled', 'helper', moveable && this.table.selectedRows.every(row => row !== firstRow));
    this.moveRowDownMenu.setPropertyDimension('visible', 'helper', moveable);
    this.moveRowDownMenu.setPropertyDimension('enabled', 'helper', moveable && this.table.selectedRows.every(row => row !== lastRow));
  }

  protected _onTableEvent(event: Event<Table>) {
    this._updateMenus();
  }

  protected _onMoveRowMenuAction(event: Event<Action>) {
    let direction: TableRowMoveDirection = event.source === this.moveRowUpMenu ? 'up' : 'down';
    let rows = this.table.selectedRows;
    if (this.selectedRowsFilter) {
      rows = rows.filter(row => this.selectedRowsFilter(row, direction));
    }
    this.table.moveRowsUpOrDown(rows, direction);
  }
}

export interface MoveRowMenuInstallOptions {
  table: Table;
  moveRowUpMenu: Menu;
  moveRowDownMenu: Menu;
  selectedRowsFilter?: (selectedRow: TableRow, direction: TableRowMoveDirection) => boolean;
}
