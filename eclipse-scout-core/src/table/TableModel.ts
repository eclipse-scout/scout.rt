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
import {
  Action, ActionModel, Column, ColumnModel, Filter, Menu, MenuModel, Status, TableCompactHandler, TableControl, TableControlModel, TableRow, TableSelectionHandler, TableTileGridMediator, TableUserFilterModel, Tile, WidgetModel
} from '../index';
import {TableCheckableStyle, TableGroupingStyle, TableHierarchicalStyle} from './Table';
import {RefModel} from '../types';
import {DropType} from '../util/dragAndDrop';
import {TableRowData} from './TableRowModel';

export default interface TableModel extends WidgetModel {
  /**
   * Configures whether the columns are auto resized. Default is false.
   * If true, all columns are resized so that the table never needs horizontal scrolling.
   * This is especially useful for tables inside a form.
   */
  autoResizeColumns?: boolean;
  columnAddable?: boolean;
  columns?: Column<any>[] | RefModel<ColumnModel<any>>[];
  /**
   * Configures whether the table is checkable. Default is false.
   */
  checkable?: boolean;
  checkableStyle?: TableCheckableStyle;
  /**
   * Configures whether the table should be in compact mode. Default is false.
   */
  compact?: boolean;
  compactHandler?: TableCompactHandler;
  /**
   * Configures the drop support of this table. One of {@link dragAndDrop.SCOUT_TYPES}. Default is none.
   */
  dropType?: DropType;
  /**
   * Configures the maximum size for a drop request (in bytes).
   * Default is {@link dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE}.
   */
  dropMaximumSize?: number;
  groupingStyle?: TableGroupingStyle;
  tableStatus?: Status;
  /**
   * Configures whether the header row is enabled. Default is true.
   * In a disabled header it is not possible to move or resize the columns and the table header menu cannot be opened.
   */
  headerEnabled?: boolean;
  /**
   * Configures whether the header row is visible. Default is true. The header row contains the titles of each column.
   */
  headerVisible?: boolean;
  /**
   * Configures whether the header menus are enabled. Default is true.
   * When header menus are disabled, a click on the header will toggle between ascending and descending sorting instead of opening the header popup.
   */
  headerMenusEnabled?: boolean;
  hierarchical?: boolean;
  hierarchicalStyle?: TableHierarchicalStyle;
  keyStrokes?: Action[] | RefModel<ActionModel>[];
  menus?: Menu[] | RefModel<MenuModel>[];
  menuBarVisible?: boolean;
  /**
   * Configures whether only one row can be checked in this table. Default is true.
   * This configuration is only useful if {@link checkable} is true.
   */
  multiCheck?: boolean;
  /**
   * Configures whether more than one row can be selected at once in this table. Default is true.
   */
  multiSelect?: boolean;
  /**
   * Configures whether the table supports multiline text. Default is false.
   * If multiline text is supported and a string column has set the {@link Column.textWrap} property to true, the text is wrapped and uses two or more lines.
   */
  multilineText?: boolean;
  /**
   * Configures whether the table always scrolls to the selection. Default is false.
   * When activated and the selection in a table changes, the table is scrolled to the selection so that the selected row is visible.
   * The selection is also revealed on row order changes (e.g. when the table is sorted or rows are inserted above the selected row).
   */
  scrollToSelection?: boolean;
  /**
   * Rows or row Ids of the selected rows.
   */
  selectedRows?: TableRow[] | string[];
  /**
   * Configures whether sort is enabled for this table. Default is true.
   * If sort is enabled, the table rows are sorted based on their sort index (see {@link Column.sortIndex}) and the user might change the sorting at run time.
   * If sort is disabled, the table rows are not sorted and the user cannot change the sorting.
   */
  sortEnabled?: boolean;
  tableControls?: TableControl[] | RefModel<TableControlModel>[];
  /**
   * Configures the visibility of the table status. Default is false (invisible).
   */
  tableStatusVisible?: boolean;
  tableTileGridMediator?: TableTileGridMediator;
  /**
   * Configures whether the table tile mode is enabled by default. Default is false.
   */
  tileMode?: boolean;
  /**
   * Necessary to display the table in tile mode (see {@link tileMode}).
   * Used to create a tile for a TableRow.
   */
  tileProducer?: (row?: TableRow) => Tile;
  /**
   * Specifies if the table footer is visible. Default is false.
   */
  footerVisible?: boolean;
  filters?: Filter<TableRow>[] | RefModel<TableUserFilterModel>[];
  rows?: (TableRow | TableRowData)[];
  maxRowCount?: number;
  /**
   * Configures whether the table shows tooltips if the cell content is truncated.
   * Possible values:
   *  <ul>
   *    <li>true if the tooltip should always be shown if the cell content is truncated.</li>
   *    <li>false if the tooltip should never be shown.</li>
   *    <li>null cell tooltip is only shown if it is not possible to resize the column.</li>
   *  </ul>
   *  Default is null.
   */
  truncatedCellTooltipEnabled?: boolean;
  uiCssClass?: string;
  rowLevelPadding?: number;
  rowHeight?: number;
  rowWidth?: number;
  /**
   * Configures whether the row icon is visible.
   *
   * If set to true the gui creates a column which contains the row icons. The column has a fixed width (see
   * {@link rowIconColumnWidth}), is not movable and always the first column (or. the second if the table is checkable).
   * The column is not available in the model.
   *
   * If you need other settings or if you need the icon at another column position, you cannot use the row icons.
   * Instead, you have to create a column and use {@link Cell.iconId} to set the icons on its cells.
   */
  rowIconVisible?: boolean;
  /**
   * Configures the row icon column width.
   * Has only an effect if {@link rowIconVisible} is true.
   */
  rowIconColumnWidth?: number;
  staticMenus?: Menu[] | RefModel<MenuModel>[];
  selectionHandler?: TableSelectionHandler;
  /**
   * Virtual relates to the term "Virtual Scrolling". This means, only the table rows in the view port and some more will be
   * rendered. The others will be rendered as soon as they will be moved into the view port, either by scrolling or by
   * any other action like sorting, filtering etc. This can lead to a big performance boost when having many rows.
   *
   * Default is true.
   */
  virtual?: boolean;
  /**
   * If enabled, a text field is shown when the table is focused or hovered so the user can filter the table rows by typing.
   * Default is true.
   */
  textFilterEnabled?: boolean;
}
