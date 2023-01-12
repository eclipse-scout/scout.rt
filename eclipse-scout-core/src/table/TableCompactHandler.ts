/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BooleanColumn, Cell, Column, CompactBean, CompactLine, Event, EventHandler, InitModelOf, objects, ObjectWithType, SomeRequired, Table, TableCompactHandlerModel, TableRow, TableRowsInsertedEvent, TableRowsUpdatedEvent
} from '../index';

export class TableCompactHandler implements TableCompactHandlerModel, ObjectWithType {
  declare model: TableCompactHandlerModel;
  declare initModel: SomeRequired<this['model'], 'table'>;

  objectType: string;
  table: Table;
  useOnlyVisibleColumns: boolean;
  maxContentLines: number;
  protected _oldStates: Record<string, any>;
  protected _updateHandler: EventHandler<TableRowsInsertedEvent | TableRowsUpdatedEvent | Event<Table>>;

  constructor() {
    this.table = null;
    this.useOnlyVisibleColumns = true;
    this.maxContentLines = 3;
    this._oldStates = objects.createMap();
    this._updateHandler = null;
  }

  init(model: InitModelOf<this>) {
    $.extend(this, model);
  }

  setUseOnlyVisibleColumns(useOnlyVisibleColumns: boolean) {
    this.useOnlyVisibleColumns = useOnlyVisibleColumns;
  }

  setMaxContentLines(maxContentLines: number) {
    this.maxContentLines = maxContentLines;
  }

  handle(compact: boolean) {
    if (compact) {
      this._compactColumns(true);
      this._attachTableHandler();
    } else {
      this._detachTableHandler();
      this._compactColumns(false);
    }
    this._adjustTable(compact);
    if (compact) {
      this.updateValues(this.table.rows);
    }
  }

  protected _adjustTable(compact: boolean) {
    if (compact) {
      this._cacheAndSetProperty('headerVisible', () => this.table.headerVisible, () => this.table.setHeaderVisible(false));
      this._cacheAndSetProperty('autoResizeColumns', () => this.table.autoResizeColumns, () => this.table.setAutoResizeColumns(true));
    } else {
      this._resetProperty('headerVisible', value => this.table.setHeaderVisible(value));
      this._resetProperty('autoResizeColumns', value => this.table.setAutoResizeColumns(value));
    }
  }

  protected _cacheAndSetProperty(propertyName: string, getter: () => any, setter: () => void) {
    if (objects.isNullOrUndefined(this._oldStates[propertyName])) {
      this._oldStates[propertyName] = getter();
    }
    setter();
  }

  protected _resetProperty(propertyName: string, setter: (oldValue: any) => void) {
    let oldState = this._oldStates[propertyName];
    if (!objects.isNullOrUndefined(oldState)) {
      setter(oldState);
      delete this._oldStates[propertyName];
    }
  }

  protected _compactColumns(compact: boolean) {
    this.table.displayableColumns(false).forEach(column => column.setCompacted(compact, false));
    this.table.onColumnVisibilityChanged();
  }

  protected _attachTableHandler() {
    if (this._updateHandler == null) {
      this._updateHandler = this._onTableEvent.bind(this);
      this.table.on('rowsInserted rowsUpdated columnStructureChanged', this._updateHandler);
    }
  }

  protected _detachTableHandler() {
    if (this._updateHandler != null) {
      this.table.off('rowsInserted rowsUpdated columnStructureChanged', this._updateHandler);
      this._updateHandler = null;
    }
  }

  updateValues(rows: TableRow[]) {
    if (rows.length === 0) {
      return;
    }
    let columns = this._getColumns();
    rows.forEach(row => this._updateValue(columns, row));
  }

  protected _updateValue(columns: Column<any>[], row: TableRow) {
    row.setCompactValue(this.buildValue(columns, row));
  }

  buildValue(columns: Column<any>[], row: TableRow): string {
    return this._buildValue(this._createBean(columns, row));
  }

  protected _createBean(columns: Column<any>[], row: TableRow): CompactBean {
    let bean = new CompactBean();
    this._processColumns(columns, row, bean);
    this._postProcessBean(bean);
    return bean;
  }

  protected _processColumns(columns: Column<any>[], row: TableRow, bean: CompactBean) {
    columns.forEach((column, i) => this._processColumn(column, i, row, bean));
  }

  protected _getColumns(): Column<any>[] {
    return this.table.filterColumns(column => this._acceptColumn(column));
  }

  protected _acceptColumn(column: Column<any>): boolean {
    return !column.guiOnly && (!this.useOnlyVisibleColumns || (column.visible && column.displayable));
  }

  protected _processColumn(column: Column<any>, index: number, row: TableRow, bean: CompactBean) {
    this._updateBean(bean, column, index, row);
  }

  /**
   * @param bean
   *          the bean for the current row
   * @param column
   *          the currently processed column
   * @param index
   *          visible column index of the currently processed column
   * @param row
   *          the current row
   */
  protected _updateBean(bean: CompactBean, column: Column<any>, index: number, row: TableRow) {
    if (this._acceptColumnForTitle(column, index)) {
      bean.setTitleLine(this._createCompactLine(column, index, row));
    } else if (this._acceptColumnForTitleSuffix(column, index)) {
      bean.setTitleSuffixLine(this._createCompactLine(column, index, row));
    } else if (this._acceptColumnForSubtitle(column, index)) {
      bean.setSubtitleLine(this._createCompactLine(column, index, row));
    } else {
      bean.addContentLine(this._createCompactLine(column, index, row));
    }
  }

  protected _acceptColumnForTitle(column: Column<any>, index: number): boolean {
    return index === 0;
  }

  protected _acceptColumnForSubtitle(column: Column<any>, index: number): boolean {
    return index === 1;
  }

  protected _acceptColumnForTitleSuffix(column: Column<any>, index: number): boolean {
    return false;
  }

  protected _createCompactLine(column: Column<any>, index: number, row: TableRow): CompactLine {
    let headerCell: Cell;
    if (this._showLabel(column, index, row)) {
      headerCell = column.headerCell();
    }
    let cell = column.cell(row);
    let line = new CompactLine(headerCell, cell);
    this._adaptCompactLine(line, column, headerCell, cell);
    return line;
  }

  protected _showLabel(column: Column<any>, index: number, row: TableRow): boolean {
    return !this._acceptColumnForTitle(column, index) && !this._acceptColumnForSubtitle(column, index) && !this._acceptColumnForTitleSuffix(column, index);
  }

  protected _adaptCompactLine<TValue>(line: CompactLine, column: Column<TValue>, headerCell: Cell<TValue>, cell: Cell<TValue>) {
    if (column instanceof BooleanColumn) {
      let text = '';
      let value = cell.value as boolean;
      if (value) {
        text = 'X';
      } else if (value === null) {
        text = '?';
      }
      line.textBlock.setText(text);
    }
  }

  protected _postProcessBean(bean: CompactBean) {
    bean.transform({maxContentLines: this.maxContentLines});

    // If only title is set move it to content. A title without content does not look good.
    if (bean.title && !bean.subtitle && !bean.titleSuffix && !bean.content) {
      bean.setContent(bean.title);
      bean.setTitle('');
    }
  }

  protected _buildValue(bean: CompactBean): string {
    let hasHeader = (bean.title + bean.titleSuffix + bean.subtitle) ? ' has-header' : '';
    let moreLink = bean.moreContent ? `<div class="compact-cell-more"><span class="more-link link">${this.table.session.text('More')}</span></div>` : '';

    return `
<div class="compact-cell-header">
  <div class="compact-cell-title">
    <span class="left">${bean.title}</span>
    <span class="right">${bean.titleSuffix}</span>
    </div>
  <div class="compact-cell-subtitle">${bean.subtitle}</div>
</div>
<div class="compact-cell-content${hasHeader}">${bean.content}</div>
<div class="compact-cell-more-content hidden${hasHeader}">${bean.moreContent}</div>
  ${moreLink}`;
  }

  protected _onTableEvent(event: TableRowsInsertedEvent | TableRowsUpdatedEvent | Event<Table>) {
    let rows: TableRow[];
    if (event.type === 'columnStructureChanged') {
      rows = this.table.rows;
    } else {
      rows = (event as TableRowsInsertedEvent | TableRowsUpdatedEvent).rows;
    }
    this.updateValues(rows);
  }
}
