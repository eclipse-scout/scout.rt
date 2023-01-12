/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment, Column, ColumnComparator, ObjectModel, Session, Table} from '../../index';

export interface ColumnModel<TValue = string> extends ObjectModel<Column<TValue>> {
  session?: Session;
  /**
   * Configures whether the column width is auto optimized.
   * If true: whenever the table content changes, the optimized column width is automatically calculated so that all column content is displayed without cropping.
   * Default is false.
   *
   * Keep in mind that if images (png, jpg etc., not font icons) are used, every image will have to be loaded in order to calculate the width properly.
   * Normally, only the images in the viewport are loaded initially, the others are loaded while scrolling.
   * This means if you have a lot of different images in a table it will create a lot of requests when the column is auto optimized.
   * If the images are cached (which is the default for static images), it will only happen the first time the image is loaded.
   * So if you have a lot of images you should consider manually setting a width instead of using this property.
   *
   * This may display a horizontal scroll bar on the table.
   */
  autoOptimizeWidth?: boolean;

  /**
   * Specifies the index in {@link TableRow.cells} that contains the data for this {@link Column}.
   * The default value is the same as the index of this {@link Column} in {@link Table.columns}.
   * Use this property if the column order of the data is different from the initial visible column order.
   *
   * This property has nothing to do with the visible position of the column in the {@link Table}.
   * The initial visible position is given by the index of this {@link Column} in {@link Table.columns} but may be changed by the user at anytime.
   */
  index?: number;

  checkable?: boolean;

  /**
   * Configures the maximum width of this column when auto optimized. The user can still make the column wider, though.
   * The default is -1 which means no max width.
   */
  autoOptimizeMaxWidth?: number;

  /**
   * Configures custom css class(es) of this column.
   */
  cssClass?: string;

  /**
   * Default is false.
   */
  compacted?: boolean;

  /**
   * Configures whether this column is editable or not. A user might directly modify the value of an editable column. A
   * non-editable column is read-only. Default is false.
   */
  editable?: boolean;

  /**
   * Default is false.
   */
  removable?: boolean;

  /**
   * Default is false.
   */
  modifiable?: boolean;

  /**
   * Configures whether the column width is fixed, meaning that it is not changed by resizing/auto-resizing and cannot
   * be resized by the user. Default is false.
   */
  fixedWidth?: boolean;

  /**
   * Configures whether the column position is fixed, meaning that it cannot be moved by the user. Also, other columns
   * cannot be moved beyond a fixed column. Default is false.
   */
  fixedPosition?: boolean;

  /**
   * Configures if the table is grouped by this column. Default is false.
   */
  grouped?: boolean;

  /**
   * Configures the header css class(es) of this column.
   * A string containing one or more classes separated by space, or null if no css class should be set.
   * Default is null.
   */
  headerCssClass?: string;

  /**
   * Configures the header icon of this column. Default is none.
   */
  headerIconId?: string;

  /**
   * Configures, if HTML rendering is enabled for this column header. Default is false.
   * Make sure that any user input (or other insecure input) is encoded (security), if this property is enabled.
   */
  headerHtmlEnabled?: boolean;

  /**
   * Configures the header tooltip of this column. Default is none.
   */
  headerTooltipText?: string;

  /**
   * Configures the background color of this column header. The color is represented by the HEX value (e.g. 'FFFFFF').
   */
  headerBackgroundColor?: string;

  /**
   * Configures the color of this column header text. The color is represented by the HEX value (e.g. 'FFFFFF').
   */
  headerForegroundColor?: string;

  /**
   * Configures the font of this column header text.
   */
  headerFont?: string;

  /**
   * Configures, if HTML rendering is enabled for the header tooltip. Default is false.
   * Make sure that any user input (or other insecure input) is encoded (security), if this property is enabled.
   */
  headerTooltipHtmlEnabled?: boolean;

  /**
   * Configures the horizontal alignment of text inside this column (including header text).
   * Default is -1 (left alignment).
   */
  horizontalAlignment?: Alignment;

  /**
   * Configures, if HTML rendering is enabled for this column. Default is false.
   * Make sure that any user input (or other insecure input) is encoded (security), if this property is enabled.
   */
  htmlEnabled?: boolean;

  /**
   * Configures whether this column value is mandatory (required). This only affects editable columns (see {@link editable}). Default is false.
   */
  mandatory?: boolean;

  /**
   * If sorting for this column is active. Default is false.
   */
  sortActive?: boolean;

  nodeColumnCandidate?: boolean;

  guiOnly?: boolean;

  /**
   * Configures whether this column is sorted ascending or descending.
   * For a column to be sorted at all, a sort index must be set (see {@link sortIndex}).
   * Default is true.
   */
  sortAscending?: boolean;

  /**
   * Configures the sort index of this column. A sort index < 0 means that the column is not considered for sorting.
   * Several columns might have set a sort index. Sorting starts with the column having the lowest sort index (>= 0).
   * Default is -1 (no sorting).
   */
  sortIndex?: number;

  /**
   * Configures whether this column is a summary column. Summary columns are used in case of a table with children.
   * The label of the child node is based on the value of the summary columns.
   * Default is false.
   */
  summary?: boolean;

  type?: string;

  /**
   * Configures the width of this column. The width of a column is represented by an integer value. Default is 60.
   *
   * If the table's auto resize flag is not set, the column's width is represented by the configured width.
   *
   * If the table's auto resize flag is set (see {@link autoOptimizeWidth}), the ratio of the column widths determines the real column width.
   * Additionally, these configured column width acts as minimum width so the calculated real column width will never be smaller than this configured value.
   * This allows the table to be displayable on small screens. The user can still make the column smaller, though.
   *
   * Note if you want to have a very small column, you may have to adjust the minimum width as well.
   */
  width?: number;

  /**
   * the width the column initially has. Default is the same as {@link width}.
   */
  initialWidth?: number;

  /**
   * Configures the minimum width of this column. With this value you can control how small the user can make the column.
   */
  minWidth?: number;

  /**
   * Default is true.
   */
  showSeparator?: boolean;

  /**
   * The table this column belongs to.
   */
  table?: Table;

  /**
   * Default is 4000.
   */
  maxLength?: number;

  /**
   * Configures the header text of this column. Default is no text.
   */
  text?: string;

  /**
   * Default is false.
   */
  textWrap?: boolean;

  /**
   * Default is {@link TextColumnUserFilter}.
   */
  filterType?: string;

  /**
   * Comparator used for sorting. Default is {@link comparators.TEXT}
   */
  comparator?: ColumnComparator<string>;

  /**
   * Configures whether the column is displayable or not. A non-displayable column is always invisible for the user.
   * Default is true.
   * A displayable column may be visible for a user, depending on {@link visible}.
   * However, if it is displayable, the user can show the column even it is initially hidden.
   */
  displayable?: boolean;

  /**
   * Configures the initial visibility of this column. Default is true.
   * If the column must be visible for the user, it must be displayable too (see {@link displayable}).
   */
  visible?: boolean;

  textBased?: boolean;

  /**
   * Configures, if the header menu is enabled for this column header. Default is true.
   * When header menu is disabled, a click on the header will toggle between ascending and descending sorting instead of opening the header popup.
   */
  headerMenuEnabled?: boolean;

  [property: string]: any; // allow custom properties
}
