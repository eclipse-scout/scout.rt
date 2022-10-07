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
import {Cell, Column, Event, Filter, KeyStroke, Menu, NumberColumn, PropertyChangeEvent, Status, Table, TableControl, TableRow, Tile, TileTableHeaderBox, ValueField, WidgetEventMap} from '../index';
import {TableCheckableStyle, TableGroupingStyle, TableHierarchicalStyle} from './Table';
import {DragAndDropType, FileDropEvent} from '../util/dragAndDrop';

export interface TableColumnBackgroundEffectChangedEvent<T extends Table = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAggregationFunctionChangedEvent<T extends Table = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAllRowsDeletedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableAppLinkActionEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  row: TableRow;
  ref: string;
  $appLink: JQuery;
}

export interface TableCancelCellEditEvent<T extends Table = Table> extends Event<T> {
  field: ValueField;
  row: TableRow;
  column: Column;
  cell: Cell;
}

export interface TableColumnMovedEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  oldPos: number;
  newPos: number;
  dragged: boolean;
}

export interface TableColumnResizedEvent<T extends Table = Table> extends Event<T> {
  column: Column;
}

export interface TableColumnResizedToFitEvent<T extends Table = Table> extends Event<T> {
  column: Column;
}

export interface TableCompleteCellEditEvent<T extends Table = Table> extends Event<T> {
  field: ValueField;
  row: TableRow;
  column: Column;
  cell: Cell;
}

export interface TableDropEvent<T extends Table = Table> extends Event<T>, FileDropEvent {
}

export interface TableFilterAddedEvent<T extends Table = Table> extends Event<T> {
  filter: Filter<TableRow>;
}

export interface TableFilterRemovedEvent<T extends Table = Table> extends Event<T> {
  filter: Filter<TableRow>;
}

export interface TableFiltersRemovedEvent<T extends Table = Table> extends Event<T> {
  filter: Filter<TableRow>;
}

export interface TableGroupEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  groupAscending: boolean;
  groupingRemoved?: boolean;
  multiGroup?: boolean;
  groupingRequested?: boolean;
}

export interface TablePrepareCellEditEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  row: TableRow;
}

export interface TableReloadEvent<T extends Table = Table> extends Event<T> {
  reloadReason: string;
}

export interface TableRowActionEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  row: TableRow;
}

export interface TableRowClickEvent<T extends Table = Table> extends Event<T> {
  originalEvent: JQuery.MouseUpEvent;
  row: TableRow;
  mouseButton: number;
  column: Column;
}

export interface TableRowInitEvent<T extends Table = Table> extends Event<T> {
  row: TableRow;
}

export interface TableRowOrderChangedEvent<T extends Table = Table> extends Event<T> {
  row: TableRow;
  animating: boolean;
}

export interface TableRowsCheckedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsDeletedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsExpandedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsInsertedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableRowsSelectedEvent<T extends Table = Table> extends Event<T> {
  debounce: boolean;
}

export interface TableRowsUpdatedEvent<T extends Table = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableSortEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  sortAscending: boolean;
  sortingRemoved?: boolean;
  multiSort?: boolean;
  sortingRequested?: boolean;
}

export interface TableStartCellEditEvent<T extends Table = Table> extends Event<T> {
  column: Column;
  row: TableRow;
  field: ValueField;
}

export interface TableColumnOrganizeActionEvent<T extends Table = Table> extends Event<T> {
  action: 'add' | 'remove' | 'modify';
  column: Column;
}

export default interface TableEventMap extends WidgetEventMap {
  'aggregationFunctionChanged': TableAggregationFunctionChangedEvent;
  'allRowsDeleted': TableAllRowsDeletedEvent;
  'appLinkAction': TableAppLinkActionEvent;
  'cancelCellEdit': TableCancelCellEditEvent;
  'clipboardExport': Event<Table>;
  'columnMoved': TableColumnMovedEvent;
  'columnResized': TableColumnResizedEvent;
  'columnResizedToFit': TableColumnResizedToFitEvent;
  'columnStructureChanged': Event<Table>;
  'completeCellEdit': TableCompleteCellEditEvent;
  'drop': TableDropEvent;
  'filter': Event<Table>;
  'filterAdded': TableFilterAddedEvent;
  'filterRemoved': TableFilterRemovedEvent;
  'filterReset': Event<Table>;
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
  'statusChanged': Event<Table>;
  'columnBackgroundEffectChanged': TableColumnBackgroundEffectChangedEvent;
  'columnOrganizeAction': TableColumnOrganizeActionEvent;
  'propertyChange:autoResizeColumns': PropertyChangeEvent<boolean, Table>;
  'propertyChange:checkable': PropertyChangeEvent<boolean, Table>;
  'propertyChange:checkableStyle': PropertyChangeEvent<TableCheckableStyle, Table>;
  'propertyChange:compact': PropertyChangeEvent<boolean, Table>;
  'propertyChange:contextColumn': PropertyChangeEvent<Column, Table>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number, Table>;
  'propertyChange:dropType': PropertyChangeEvent<DragAndDropType, Table>;
  'propertyChange:footerVisible': PropertyChangeEvent<boolean, Table>;
  'propertyChange:groupingStyle': PropertyChangeEvent<TableGroupingStyle, Table>;
  'propertyChange:headerEnabled': PropertyChangeEvent<boolean, Table>;
  'propertyChange:headerMenusEnabled': PropertyChangeEvent<boolean, Table>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean, Table>;
  'propertyChange:hierarchical': PropertyChangeEvent<boolean, Table>;
  'propertyChange:hierarchicalStyle': PropertyChangeEvent<TableHierarchicalStyle, Table>;
  'propertyChange:keyStrokes': PropertyChangeEvent<KeyStroke[], Table>;
  'propertyChange:menuBarVisible': PropertyChangeEvent<boolean, Table>;
  'propertyChange:menus': PropertyChangeEvent<Menu[], Table>;
  'propertyChange:multiCheck': PropertyChangeEvent<boolean, Table>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean, Table>;
  'propertyChange:multilineText': PropertyChangeEvent<boolean, Table>;
  'propertyChange:rowIconColumnWidth': PropertyChangeEvent<number, Table>;
  'propertyChange:rowIconVisible': PropertyChangeEvent<boolean, Table>;
  'propertyChange:rowLevelPadding': PropertyChangeEvent<number, Table>;
  'propertyChange:scrollToSelection': PropertyChangeEvent<boolean, Table>;
  'propertyChange:selectedRows': PropertyChangeEvent<TableRow[], Table>;
  'propertyChange:sortEnabled': PropertyChangeEvent<boolean, Table>;
  'propertyChange:staticMenus': PropertyChangeEvent<Menu[], Table>;
  'propertyChange:tableControls': PropertyChangeEvent<TableControl[], Table>;
  'propertyChange:tableStatus': PropertyChangeEvent<Status, Table>;
  'propertyChange:tableStatusVisible': PropertyChangeEvent<boolean, Table>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean, Table>;
  'propertyChange:tileMode': PropertyChangeEvent<boolean, Table>;
  'propertyChange:tileProducer': PropertyChangeEvent<(row: TableRow) => Tile, Table>;
  'propertyChange:tileTableHeader': PropertyChangeEvent<TileTableHeaderBox, Table>;
  'propertyChange:truncatedCellTooltipEnabled': PropertyChangeEvent<boolean, Table>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number, Table>;
  'propertyChange:virtual': PropertyChangeEvent<boolean, Table>;
}
