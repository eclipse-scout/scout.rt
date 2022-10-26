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
import $ from 'jquery';
import {AggregateTableRow} from './Table';
import {Cell, EnumObject, LookupRow, Table, TableRowModel} from '../index';

export type TableRowStatus = EnumObject<typeof TableRow.Status>;

export default class TableRow implements TableRowModel {
  declare model: TableRowModel;

  cells: Cell[];
  checked: boolean;
  compactValue: string;
  enabled: boolean;
  filterAccepted: boolean;
  height: number;
  hasError: boolean;
  id: string;
  initialized: boolean;
  iconId: string;
  parentRow: TableRow;
  parent: Table;
  childRows: TableRow[];
  expanded: boolean;
  status: TableRowStatus;
  dataMap?: Record<PropertyKey, any>;
  hierarchyLevel: number;
  cssClass: string;
  aggregateRowAfter: AggregateTableRow;
  aggregateRowBefore: AggregateTableRow;
  lookupRow: LookupRow<any>;
  $row: JQuery;

  protected _expandable: boolean;

  constructor() {
    this.$row = null;
    this.aggregateRowAfter = null;
    this.cells = [];
    this.checked = false;
    this.compactValue = null;
    this.enabled = true;
    this.filterAccepted = true;
    this.height = null;
    this.hasError = false;
    this.id = null;
    this.initialized = false;
    this.parentRow = null;
    this.parent = null;
    this.childRows = [];
    this.expanded = false;
    this.status = TableRow.Status.NON_CHANGED;
    this.hierarchyLevel = 0;
  }

  static Status = {
    NON_CHANGED: 'nonChanged',
    INSERTED: 'inserted',
    UPDATED: 'updated'
  } as const;

  init(model: TableRowModel) {
    this._init(model);
    this.initialized = true;
  }

  protected _init(model: TableRowModel) {
    if (!model.parent) {
      throw new Error('missing property \'parent\'');
    }
    $.extend(this, model);
    this._initCells();
  }

  protected _initCells() {
    this.getTable().columns.forEach(column => {
      if (!column.guiOnly) {
        let cell = this.cells[column.index];
        cell = column.initCell(cell, this);
        this.cells[column.index] = cell;
      }
    });
  }

  animateExpansion() {
    let $row = this.$row;
    if (!$row) {
      return;
    }
    let $rowControl = $row.find('.table-row-control');
    if (this.expanded) {
      $rowControl.addClassForAnimation('expand-rotate');
    } else {
      $rowControl.addClassForAnimation('collapse-rotate');
    }
  }

  hasFilterAcceptedChildren(): boolean {
    return this.childRows.some(childRow => childRow.filterAccepted || childRow.hasFilterAcceptedChildren());
  }

  getTable(): Table {
    return this.parent;
  }

  setCompactValue(compactValue: string) {
    this.compactValue = compactValue;
  }

  setFilterAccepted(filterAccepted: boolean) {
    this.filterAccepted = filterAccepted;
  }
}
