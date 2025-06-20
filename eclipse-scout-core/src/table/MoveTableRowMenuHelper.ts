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
  rowFilter: (selectedRow: TableRow, direction: TableRowMoveDirection) => boolean;
  protected _actionHandler: EventHandler;
  protected _tableHandler: EventHandler;
  protected _moving = false;

  install(options: MoveRowMenuInstallOptions) {
    if (this.table) {
      // Already installed
      return;
    }
    this.table = scout.assertInstance(options.table, Table);
    this.moveRowUpMenu = scout.assertInstance(options.moveRowUpMenu, Menu);
    this.moveRowDownMenu = scout.assertInstance(options.moveRowDownMenu, Menu);
    this.rowFilter = options.rowFilter;

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
    if (this._moving) {
      // Don't update while moving, there may be other rowOrderChanged-handlers that need to update their state first because it may influence the rowFilter
      return false;
    }
    let firstRow = arrays.first(this.table.rows);
    let lastRow = arrays.last(this.table.rows);
    let movable = this.table.selectedRows.length && this.table.rows.length > 1;
    let rowFilter = this.rowFilter || Boolean;
    let movableUp = movable && this.table.selectedRows.every(row => row !== firstRow && rowFilter(row, 'up'));
    let movableDown = movable && this.table.selectedRows.every(row => row !== lastRow && rowFilter(row, 'down'));
    // Using a new dimension makes it possible to still hide and disable the menus by the consumer of this helper, even if this helper would show or enable the menus
    this.moveRowUpMenu.setPropertyDimension('visible', 'helper', movable);
    this.moveRowUpMenu.setPropertyDimension('enabled', 'helper', movableUp);
    this.moveRowDownMenu.setPropertyDimension('visible', 'helper', movable);
    this.moveRowDownMenu.setPropertyDimension('enabled', 'helper', movableDown);
  }

  protected _onTableEvent(event: Event<Table>) {
    this._updateMenus();
  }

  protected _onMoveRowMenuAction(event: Event<Action>) {
    let direction: TableRowMoveDirection = event.source === this.moveRowUpMenu ? 'up' : 'down';
    let rows = this._selectedRows(direction);
    this._moving = true;
    let movedRows = [];
    try {
      movedRows = this.table.moveRowsUpOrDown(rows, direction);
    } finally {
      this._moving = false;
    }
    if (movedRows.length) {
      this._updateMenus();
    }
  }

  protected _selectedRows(direction: TableRowMoveDirection) {
    let rows = this.table.selectedRows;
    if (this.rowFilter && !rows.every(row => this.rowFilter(row, direction))) {
      // Every row needs to be movable, don't move only some of the rows to keep the distances between rows
      rows = [];
    }
    return rows;
  }
}

export interface MoveRowMenuInstallOptions {
  table: Table;
  moveRowUpMenu: Menu;
  moveRowDownMenu: Menu;
  /**
   * May be specified if some rows should not be movable.
   */
  rowFilter?: (selectedRow: TableRow, direction: TableRowMoveDirection) => boolean;
}
