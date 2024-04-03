/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AppLinkActionEvent, Cell, Column, DropType, Event, FileDropEvent, Filter, KeyStroke, Menu, NumberColumn, PropertyChangeEvent, Status, Table, TableCheckableStyle, TableControl, TableGroupingStyle, TableHierarchicalStyle, TableReloadReason,
  TableRow, Tile, TileTableHeaderBox, ValueField, WidgetEventMap
} from '../index';

export interface TableColumnBackgroundEffectChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAggregationFunctionChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
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
  dragged: boolean;
}

export interface TableColumnResizedEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
}

export interface TableColumnResizedToFitEvent<TValue = any, T = Table> extends Event<T> {
  column: Column<TValue>;
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

export interface TableFiltersRemovedEvent<T = Table> extends Event<T> {
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
  row: TableRow;
  animating: boolean;
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

export interface TableColumnOrganizeActionEvent<TValue = any, T = Table> extends Event<T> {
  action: 'add' | 'remove' | 'modify';
  column: Column<TValue>;
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
  'columnStructureChanged': Event;
  'completeCellEdit': TableCompleteCellEditEvent;
  'drop': TableDropEvent;
  'filter': Event;
  'filterAdded': TableFilterAddedEvent;
  'filterRemoved': TableFilterRemovedEvent;
  'filterReset': Event;
  'filtersRemoved': TableFiltersRemovedEvent;
  'group': TableGroupEvent;
  'prepareCellEdit': TablePrepareCellEditEvent;
  'reload': TableReloadEvent;
  'rowAction': TableRowActionEvent;
  'rowClick': TableRowClickEvent;
  'rowInit': TableRowInitEvent;
  'rowOrderChanged': TableRowOrderChangedEvent;
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
  'columnOrganizeAction': TableColumnOrganizeActionEvent;
  'propertyChange:autoResizeColumns': PropertyChangeEvent<boolean>;
  'propertyChange:checkable': PropertyChangeEvent<boolean>;
  'propertyChange:checkableStyle': PropertyChangeEvent<TableCheckableStyle>;
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
}
