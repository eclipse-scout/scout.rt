/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AppLinkActionEvent, Cell, Column, DateColumn, DropType, Event, FileDropEvent, Filter, KeyStroke, Menu, NumberColumn, PropertyChangeEvent, Status, Table, TableAcceptRowDropEvent, TableCheckableStyle, TableControl, TableGroupingStyle,
  TableHierarchicalStyle, TableReloadReason, TableRow, TableRowDropEvent, Tile, TileTableHeaderBox, ValueField, WidgetEventMap
} from '../index';

export interface TableColumnBackgroundEffectChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAggregationFunctionChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableColumnDateGroupTypeChangedEvent<T = Table> extends Event<T> {
  column: DateColumn;
}

export interface TableAllRowsDeletedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableAppLinkActionEvent<TValue = any, T = Table> extends AppLinkActionEvent<T> {
  column: Column<TValue>;
  row: TableRow;
  $appLink: JQuery;
}

export interface TableCancelCellEditEvent<TValue = any, T = Table> extends Event<T> {
  field: ValueField<TValue>;
  row: TableRow;
  column: Column<TValue>;
  cell: Cell<TValue>;
}

export interface TableColumnMovedEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  oldPos: number;
  newPos: number;
}

export interface TableColumnResizedEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
}

export interface TableColumnResizedToFitEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
}

/**
 * Event containing the old and new list of {@link Column}s after {@link Table.columns} changed.
 */
export interface TableColumnStructureChangedEvent<T = Table> extends Event<T> {
  oldColumns: Column<any>[];
  newColumns: Column<any>[];
}

export interface TableCompleteCellEditEvent<TValue = any, T = Table> extends Event<T> {
  field: ValueField<TValue>;
  row: TableRow;
  column: Column<TValue>;
  cell: Cell<TValue>;
}

export interface TableDropEvent<T = Table> extends Event<T>, FileDropEvent {
}

export interface TableFilterAddedEvent<T = Table> extends Event<T> {
  filter: Filter<TableRow>;
}

export interface TableFilterRemovedEvent<T = Table> extends Event<T> {
  filter: Filter<TableRow>;
}

export interface TableGroupEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  groupAscending: boolean;
  groupingRemoved?: boolean;
  multiGroup?: boolean;
  groupingRequested?: boolean;
}

export interface TablePrepareCellEditEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  row: TableRow;
}

export interface TableReloadEvent<T = Table> extends Event<T> {
  reloadReason: TableReloadReason;
}

export interface TableRowActionEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  row: TableRow;
}

export interface TableRowClickEvent<TValue = any, T = Table> extends Event<T> {
  originalEvent: JQuery.MouseEventBase;
  row: TableRow;
  mouseButton: number;
  column: Column<TValue>;
}

export interface TableRowInitEvent<T = Table> extends Event<T> {
  row: TableRow;
}

export interface TableRowOrderChangedEvent<T = Table> extends Event<T> {
}

export interface TableRowOrderChangeAnimationEvent<T = Table> extends Event<T> {
  row: TableRow;
}

export interface TableRowsCheckedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsDeletedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsExpandedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsInsertedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsSelectedEvent<T = Table> extends Event<T> {
  debounce: boolean;
}

export interface TableRowsUpdatedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableSortEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  sortAscending: boolean;
  sortingRemoved?: boolean;
  multiSort?: boolean;
  sortingRequested?: boolean;
}

export interface TableStartCellEditEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
  row: TableRow;
  field: ValueField<TValue>;
}

export interface TableEventMap extends WidgetEventMap {
  'aggregationFunctionChanged': TableAggregationFunctionChangedEvent;
  'allRowsDeleted': TableAllRowsDeletedEvent;
  'appLinkAction': TableAppLinkActionEvent;
  'cancelCellEdit': TableCancelCellEditEvent;
  'clipboardExport': Event;
  'columnMoved': TableColumnMovedEvent;
  'columnResized': TableColumnResizedEvent;
  'columnResizedToFit': TableColumnResizedToFitEvent;
  'columnStructureChanged': TableColumnStructureChangedEvent;
  'completeCellEdit': TableCompleteCellEditEvent;
  'drop': TableDropEvent;
  'filter': Event;
  'filterAdded': TableFilterAddedEvent;
  'filterRemoved': TableFilterRemovedEvent;
  'group': TableGroupEvent;
  'prepareCellEdit': TablePrepareCellEditEvent;
  'reload': TableReloadEvent;
  'rowAction': TableRowActionEvent;
  'rowClick': TableRowClickEvent;
  'rowInit': TableRowInitEvent;
  /**
   * Will be triggered when the row order has changed but before the new order is rendered.
   */
  'rowOrderChanged': TableRowOrderChangedEvent;
  /**
   * Will be triggered during the row order change animation for each animation step.
   */
  'rowOrderChangeAnimation': TableRowOrderChangeAnimationEvent;
  'rowsChecked': TableRowsCheckedEvent;
  'rowsDeleted': TableRowsDeletedEvent;
  'rowsExpanded': TableRowsExpandedEvent;
  'rowsInserted': TableRowsInsertedEvent;
  'rowsSelected': TableRowsSelectedEvent;
  'rowsUpdated': TableRowsUpdatedEvent;
  'sort': TableSortEvent;
  'startCellEdit': TableStartCellEditEvent;
  'statusChanged': Event;
  'columnBackgroundEffectChanged': TableColumnBackgroundEffectChangedEvent;
  'columnDateGroupTypeChanged': TableColumnDateGroupTypeChangedEvent;
  'acceptRowDrop': TableAcceptRowDropEvent;
  'rowDrop': TableRowDropEvent;
  'afterRowDrop': TableRowDropEvent;
  'propertyChange:autoResizeColumns': PropertyChangeEvent<boolean>;
  'propertyChange:checkable': PropertyChangeEvent<boolean>;
  'propertyChange:checkableStyle': PropertyChangeEvent<TableCheckableStyle>;
  'propertyChange:columns': PropertyChangeEvent<Column<any>[]>;
  'propertyChange:compact': PropertyChangeEvent<boolean>;
  'propertyChange:contextColumn': PropertyChangeEvent<Column<any>>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number>;
  'propertyChange:dropType': PropertyChangeEvent<DropType>;
  'propertyChange:footerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:groupingStyle': PropertyChangeEvent<TableGroupingStyle>;
  'propertyChange:headerEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:headerMenusEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:hierarchical': PropertyChangeEvent<boolean>;
  'propertyChange:hierarchicalStyle': PropertyChangeEvent<TableHierarchicalStyle>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[]>;
  'propertyChange:menuBarVisible': PropertyChangeEvent<boolean>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:multiCheck': PropertyChangeEvent<boolean>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean>;
  'propertyChange:multilineText': PropertyChangeEvent<boolean>;
  'propertyChange:rowIconColumnWidth': PropertyChangeEvent<number>;
  'propertyChange:rowIconVisible': PropertyChangeEvent<boolean>;
  'propertyChange:rowLevelPadding': PropertyChangeEvent<number>;
  'propertyChange:scrollToSelection': PropertyChangeEvent<boolean>;
  'propertyChange:selectedRows': PropertyChangeEvent<TableRow[]>;
  'propertyChange:sortEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:staticMenus': PropertyChangeEvent<Menu[]>;
  'propertyChange:tableControls': PropertyChangeEvent<TableControl[]>;
  'propertyChange:tableStatus': PropertyChangeEvent<Status>;
  'propertyChange:tableStatusVisible': PropertyChangeEvent<boolean>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:tileMode': PropertyChangeEvent<boolean>;
  'propertyChange:tileProducer': PropertyChangeEvent<(row: TableRow) => Tile>;
  'propertyChange:tileTableHeader': PropertyChangeEvent<TileTableHeaderBox>;
  'propertyChange:truncatedCellTooltipEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number>;
  'propertyChange:virtual': PropertyChangeEvent<boolean>;
  'propertyChange:maxRowCount': PropertyChangeEvent<number>;
  'propertyChange:estimatedRowCount': PropertyChangeEvent<number>;
  'propertyChange:asyncLoading': PropertyChangeEvent<boolean>;
}
