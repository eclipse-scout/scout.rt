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
  Action, Column, DropType, FilterOrFunction, Menu, ObjectOrChildModel, ObjectOrModel, StatusOrModel, TableCheckableStyle, TableCompactHandler, TableControl, TableGroupingStyle, TableHierarchicalStyle, TableRow, TableSelectionHandler,
  TableTileGridMediator, TableUserFilterModel, Tile, TileTableHeaderBox, WidgetModel
} from '../index';

export interface TableModel extends WidgetModel {
  /**
   * Configures whether the columns should be automatically resized.
   *
   * If true, all columns are resized so that the table doesn't need horizontal scrolling in general.
   * This is especially useful for tables inside a form.
   *
   * The {@link Column.width} acts as weight and min-width. This means, how much a column grows or shrinks compared to the other columns depends on the used width.
   * Additionally, a column cannot shrink below {@link Column.width}. If that happens, a horizontal scroll bar appears.
   *
   * Columns with {@link Column.fixedWidth} or {@link Column.autoOptimizeWidth} are excluded from being automatically resized.
   *
   * Default is false.
   */
  autoResizeColumns?: boolean;
  columnAddable?: boolean;
  columns?: ObjectOrChildModel<Column<any>>[];
  /**
   * Configures whether the table is checkable.
   *
   * Default is false.
   */
  checkable?: boolean;
  checkableStyle?: TableCheckableStyle;
  /**
   * Configures whether the table should be in compact mode.
   *
   * In compact mode, all columns are reduced to one containing the content of the other columns.
   * This is very useful if the width of the container is limited but there is enough vertical space available (e.g. on mobile clients).
   *
   * Default is false.
   */
  compact?: boolean;
  /**
   * Controls how the table and its columns should be compacted if {@link compact} is set to true.
   *
   * By default, {@link TableCompactHandler} is used.
   */
  compactHandler?: TableCompactHandler;
  /**
   * Specifies whether it should be possible to drop elements onto the table.
   *
   * Currently, only {@link DropType.FILE_TRANSFER} is supported.
   *
   * By default, dropping is disabled.
   */
  dropType?: DropType;
  /**
   * Specifies the maximum size in bytes a file can have if it is being dropped.
   *
   * It only has an effect if {@link dropType} is set to {@link DropType.FILE_TRANSFER}.
   *
   * Default is {@link dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE}
   */
  dropMaximumSize?: number;
  groupingStyle?: TableGroupingStyle;
  tableStatus?: StatusOrModel;
  /**
   * Configures whether the header row is enabled.
   *
   * In a disabled header it is not possible to move or resize the columns and the table header menu cannot be opened.
   *
   * Default is true.
   */
  headerEnabled?: boolean;
  /**
   * Configures whether the header row is visible. The header row contains the titles of each column.
   *
   * Default is true.
   */
  headerVisible?: boolean;
  /**
   * Configures whether the header menus are enabled.
   *
   * When header menus are disabled, a click on the header will toggle between ascending and descending sorting instead of opening the header popup.
   *
   * Default is true.
   */
  headerMenusEnabled?: boolean;
  /**
   * Defines whether the table should group rows with the same {@link TableRow.parentRow} and allow the user to expand and collapse these groups.
   *
   * Default is false.
   */
  hierarchical?: boolean;
  hierarchicalStyle?: TableHierarchicalStyle;
  /**
   * Configures the keystrokes that should be registered in the current {@link keyStrokeContext}.
   *
   * Use the {@link ActionModel.keyStroke} to assign the keys that need to be pressed.
   *
   * @see KeyStrokeContext
   */
  keyStrokes?: ObjectOrChildModel<Action>[];
  /**
   * Configures the menus to be displayed in the {@link MenuBar} of the table.
   *
   * The visibility of the {@link Menu} and where it should appear depends on the used {@link Table.MenuTypes} configured in {@link Menu.menuTypes}.
   */
  menus?: ObjectOrChildModel<Menu>[];
  menuBarVisible?: boolean;
  /**
   * Configures whether only one row can be checked in this table.
   *
   * This configuration is only useful if {@link checkable} is true.
   *
   * Default is true.
   */
  multiCheck?: boolean;
  /**
   * Configures whether more than one row can be selected at once in this table.
   *
   * Default is true.
   */
  multiSelect?: boolean;
  /**
   * Configures whether the table supports multiline text.
   *
   * If multiline text is supported and a string column has set the {@link Column.textWrap} property to true, the text is wrapped and uses two or more lines.
   *
   * Default is false.
   */
  multilineText?: boolean;
  /**
   * Configures whether the table always scrolls to the selection.
   *
   * When activated and the selection in a table changes, the table is scrolled to the selection so that the selected row is visible.
   * The selection is also revealed on row order changes (e.g. when the table is sorted or rows are inserted above the selected row).
   *
   * Default is false.
   */
  scrollToSelection?: boolean;
  /**
   * Rows or row Ids of the selected rows.
   */
  selectedRows?: TableRow[] | string[];
  /**
   * Configures whether sort is enabled for this table.
   *
   * - If sort is enabled, the table rows are sorted based on their sort index (see {@link Column.sortIndex}) and the user may change the sorting at run time.
   * - If sort is disabled, the table rows are not sorted and the user cannot change the sorting.
   *
   * Default is true.
   */
  sortEnabled?: boolean;
  /**
   * Defines the controls to be displayed in the {@link TableFooter}.
   */
  tableControls?: ObjectOrChildModel<TableControl>[];
  /**
   * Configures the visibility of the table status.
   *
   * Default is false (invisible).
   */
  tableStatusVisible?: boolean;
  tableTileGridMediator?: ObjectOrChildModel<TableTileGridMediator>;
  tileTableHeader?: ObjectOrChildModel<TileTableHeaderBox>;
  /**
   * Configures whether the table tile mode is enabled by default.
   *
   * Default is false.
   */
  tileMode?: boolean;
  /**
   * Necessary to display the table in tile mode (see {@link tileMode}).
   * Used to create a tile for a TableRow.
   */
  tileProducer?: (row?: TableRow) => Tile;
  /**
   * Specifies if the table footer is visible.
   *
   * Default is false.
   */
  footerVisible?: boolean;
  /**
   * The filters control which rows are allowed to be displayed in the table.
   *
   * If one of the filters does not accept a specific row, the row won't be shown. Hence, all filters must agree to make a row visible.
   *
   * By default, there are no filters.
   *
   * @see Table.visibleRows
   * @see Table.rows
   */
  filters?: (FilterOrFunction<TableRow> | TableUserFilterModel)[];
  /**
   * The {@link TableRow}s containing {@link Cell}s to be displayed in the table.
   */
  rows?: ObjectOrModel<TableRow>[];
  /**
   * Maximum row count the user is allowed to load into this table.
   *
   * The table does not limit the number of {@link rows} based on this value, it is just used by the {@link TableFooter} to show an indicator.
   * To have an effect, {@link estimatedRowCount} and {@link hasReloadHandler} need to be set as well.
   *
   * By default, there is no limitation.
   */
  maxRowCount?: number;
  /**
   * Estimated total available row count.
   *
   * This value is just used by the {@link TableFooter} to show an indicator.
   * To have an effect, {@link maxRowCount} and {@link hasReloadHandler} need to be set as well.
   *
   * By default, there is no estimation.
   */
  estimatedRowCount?: number;
  /**
   * Controls whether a `Reload data` link should be displayed in the {@link TableFooter} that triggers a {@link TableEventMap.reload} event when clicked.
   *
   * There is no default implementation for the reload handling.
   *
   * Default is false.
   */
  hasReloadHandler?: boolean;
  /**
   * Configures whether the table shows a {@link Tooltip} if the cell content is truncated.
   *
   * Possible values:
   *
   * - true, to always show the tooltip if the cell content is truncated.
   * - false, to never show the tooltip.
   * - null, to show cell tooltip only if it is not possible to resize the column.
   *
   *  Default is null.
   */
  truncatedCellTooltipEnabled?: boolean;
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
   *
   * Has only an effect if {@link rowIconVisible} is true.
   *
   * Default is {@link Column.NARROW_MIN_WIDTH}.
   */
  rowIconColumnWidth?: number;
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
   *
   * Default is true.
   */
  textFilterEnabled?: boolean;
}
