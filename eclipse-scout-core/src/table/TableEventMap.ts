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

export interface TableColumnBackgroundEffectChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAggregationFunctionChangedEvent<T = Table> extends Event<T> {
  column: NumberColumn;
}

export interface TableAllRowsDeletedEvent<T = Table> extends Event<T> {
  rows: TableRow[];
}

export interface TableAppLinkActionEvent<T = Table> extends Event<T> {
  column: Column;
  row: TableRow;
  ref: string;
  $appLink: JQuery;
}

export interface TableCancelCellEditEvent<T = Table> extends Event<T> {
  field: ValueField;
  row: TableRow;
  column: Column;
  cell: Cell;
}

export interface TableColumnMovedEvent<T = Table> extends Event<T> {
  column: Column;
  oldPos: number;
  newPos: number;
  dragged: boolean;
}

export interface TableColumnResizedEvent<T = Table> extends Event<T> {
  column: Column;
}

export interface TableColumnResizedToFitEvent<T = Table> extends Event<T> {
  column: Column;
}

export interface TableCompleteCellEditEvent<T = Table> extends Event<T> {
  field: ValueField;
  row: TableRow;
  column: Column;
  cell: Cell;
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

export interface TableGroupEvent<T = Table> extends Event<T> {
  column: Column;
  groupAscending: boolean;
  groupingRemoved?: boolean;
  multiGroup?: boolean;
  groupingRequested?: boolean;
}

export interface TablePrepareCellEditEvent<T = Table> extends Event<T> {
  column: Column;
  row: TableRow;
}

export interface TableReloadEvent<T = Table> extends Event<T> {
  reloadReason: string;
}

export interface TableRowActionEvent<T = Table> extends Event<T> {
  column: Column;
  row: TableRow;
}

export interface TableRowClickEvent<T = Table> extends Event<T> {
  originalEvent: JQuery.MouseUpEvent;
  row: TableRow;
  mouseButton: number;
  column: Column;
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

export interface TableSortEvent<T = Table> extends Event<T> {
  column: Column;
  sortAscending: boolean;
  sortingRemoved?: boolean;
  multiSort?: boolean;
  sortingRequested?: boolean;
}

export interface TableStartCellEditEvent<T = Table> extends Event<T> {
  column: Column;
  row: TableRow;
  field: ValueField;
}

export interface TableColumnOrganizeActionEvent<T = Table> extends Event<T> {
  action: 'add' | 'remove' | 'modify';
  column: Column;
}

export default interface TableEventMap extends WidgetEventMap {
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
  'propertyChange:contextColumn': PropertyChangeEvent<Column>;
  'propertyChange:dropMaximumSize': PropertyChangeEvent<number>;
  'propertyChange:dropType': PropertyChangeEvent<DragAndDropType>;
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
}
