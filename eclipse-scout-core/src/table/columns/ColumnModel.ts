/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment, Column, ColumnComparator, ObjectWithUuidModel, Session, Table} from '../../index';

export interface ColumnModel<TValue = string> extends ObjectWithUuidModel<Column<TValue>> {
  session?: Session;
  /**
   * Configures whether the column width is auto optimized.
   *
   * If true: whenever the table content changes, the optimized column width is automatically calculated so that all column content is displayed without cropping.
   *
   * Keep in mind that if images (png, jpg etc., not font icons) are used, every image will have to be loaded in order to calculate the width properly.
   * Normally, only the images in the viewport are loaded initially, the others are loaded while scrolling.
   * This means if you have a lot of different images in a table it will create a lot of requests when the column is auto optimized.
   * If the images are cached (which is the default for static images), it will only happen the first time the image is loaded.
   * So if you have a lot of images you should consider manually setting a width instead of using this property.
   *
   * This may display a horizontal scroll bar on the table.
   *
   * Default is false.
   */
  autoOptimizeWidth?: boolean;

  /**
   * Configures the maximum width of this column when auto optimized. The user can still make the column wider, though.
   *
   * The default is -1 which means no max width.
   */
  autoOptimizeMaxWidth?: number;

  /**
   * Specifies the index in {@link TableRow.cells} that contains the data for this {@link Column}.
   *
   * The default value is the same as the index of this {@link Column} in {@link Table.columns}.
   * Use this property if the column order of the data is different from the initial visible column order.
   *
   * This property has nothing to do with the visible position of the column in the {@link Table}.
   * The initial visible position is given by the index of this {@link Column} in {@link Table.columns} but may be changed by the user at anytime.
   */
  index?: number;

  /**
   * Configures whether this column value belongs to the primary key of the surrounding {@link Table}. The {@link Table}'s primary key
   * might consist of several columns.
   *
   * This is mainly used to restore the selection when rows are replaced using {@link Table.replaceRows}.
   *
   * Default is `false`.
   */
  primaryKey?: boolean;

  checkable?: boolean;

  /**
   * Configures custom css class(es) of this column.
   */
  cssClass?: string;

  /**
   * Configures whether this column is editable or not.
   *
   * Editable means that a user can directly modify the value of an editable column. A non-editable column is read-only.
   *
   * Default is false.
   */
  editable?: boolean;

  /**
   * Specifies whether the column can generally be removed. The effective value is determined by the
   * {@link TableOrganizer table organizer}. The default value is true.
   */
  removable?: boolean;

  /**
   * Specifies whether the column can generally be modified. The effective value is determined by the
   * {@link TableOrganizer table organizer}. The default value is true.
   */
  modifiable?: boolean;

  /**
   * Configures whether the column width is fixed, meaning that it is not changed by resizing/auto-resizing and cannot be resized by the user.
   *
   * Default is false.
   */
  fixedWidth?: boolean;

  /**
   * Configures whether the column position is fixed, meaning that it cannot be moved by the user. Also, other columns
   * cannot be moved beyond a fixed column.
   *
   * Default is false.
   */
  fixedPosition?: boolean;

  /**
   * Configures if the table is grouped by this column.
   *
   * Default is false.
   */
  grouped?: boolean;

  /**
   * Configures the header css class(es) of this column using a string containing one or more classes separated by space, or null if no css class should be set.
   *
   * Default is null.
   */
  headerCssClass?: string;

  /**
   * Configures the header icon of this column.
   *
   * Default is none.
   */
  headerIconId?: string;

  /**
   * Configures whether HTML tags in the column header should be interpreted or encoded.
   *
   * - If set to false, the HTML tags in the {@link text} will be encoded, so the tags won't have any effect and only plain text will be displayed.
   * - If set to true, the HTML tags in the {@link text} will be interpreted.
   *   In that case, you have to make sure that user input is encoded by yourself.
   *   E.g. if the {@link text} should display text from an input field, use {@link strings.encode} to prevent HTML injection.
   *
   * Default is false.
   */
  headerHtmlEnabled?: boolean;

  /**
   * Configures the header tooltip of this column.
   *
   * Default is none.
   */
  headerTooltipText?: string;

  /**
   * Sets the background color of the column header using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link headerCssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the header will always have that color even if it is hovered, selected etc.
   */
  headerBackgroundColor?: string;

  /**
   * Sets the foreground color of the column header text using the HTML style attribute.
   *
   * It can either be a hex based color value (without #), a color name or a color function, see https://developer.mozilla.org/en-US/docs/Web/CSS/color_value.
   *
   * *Note*: this property should be avoided in general in favor of {@link headerCssClass}, because the style attribute will always override the rules defined by the stylesheet.
   * This means, if a color is specified by this property, the header will always have that color even if it is hovered, selected etc.
   */
  headerForegroundColor?: string;

  /**
   * Sets the font of the column header text using the HTML style attribute.
   *
   * *Note*: this property should be avoided in general in favor of {@link headerCssClass}, because the style attribute will always override the rules defined by the stylesheet.
   */
  headerFont?: string;

  /**
   * Configures whether HTML rendering is enabled for the {@link headerTooltipText}.
   *
   * Make sure that any user input (or other insecure input) is encoded (security), if this property is enabled. See also {@link headerHtmlEnabled}.
   *
   * Default is false.
   */
  headerTooltipHtmlEnabled?: boolean;

  /**
   * Configures the horizontal alignment of text inside this column (including header text).
   *
   * Default is -1 (left alignment).
   */
  horizontalAlignment?: Alignment;

  /**
   * Configures whether HTML tags in the cells of this column should be interpreted or encoded.
   *
   * - If set to false, the HTML tags in the {@link Cell.text} will be encoded, so the tags won't have any effect and only plain text will be displayed.
   * - If set to true, the HTML tags in the {@link Cell.text} will be interpreted.
   *   In that case, you have to make sure that user input is encoded by yourself.
   *   E.g. if the {@link Cell.text} should display text from an input field, use {@link strings.encode} to prevent HTML injection.
   *
   * Default is false.
   */
  htmlEnabled?: boolean;

  /**
   * Configures whether this column value is mandatory (required). This only affects editable columns (see {@link editable}).
   *
   * Default is false.
   */
  mandatory?: boolean;

  /**
   * Configures whether the table should consider this column for sorting.
   *
   * Default is false.
   */
  sortActive?: boolean;

  nodeColumnCandidate?: boolean;

  guiOnly?: boolean;

  /**
   * Configures whether this column is sorted ascending or descending.
   *
   * For a column to be sorted at all, a sort index must be set (see {@link sortIndex}) and {@link sortActive} must be true.
   *
   * Default is true.
   */
  sortAscending?: boolean;

  /**
   * Configures the sort index of this column. A sort index < 0 means that the column is not considered for sorting.
   *
   * Several columns might have set a sort index. Sorting starts with the column having the lowest sort index (>= 0).
   *
   * Default is -1 (no sorting).
   */
  sortIndex?: number;

  /**
   * Configures whether this column is a summary column.
   *
   * Summary columns are used by the table to compute a summary cell for each row containing all values of every summary column.
   * This is typically used by a {@link PageWithTable} for the texts of the child pages created for each row.
   *
   * Default is false.
   */
  summary?: boolean;

  type?: string;

  /**
   * Configures the width of this column. The width of a column is represented by an integer value.
   *
   * If the table's auto resize flag is not set, the column's width is represented by the configured width.
   *
   * If the table's auto resize flag is set (see {@link autoOptimizeWidth}), the ratio of the column widths determines the real column width.
   * Additionally, these configured column width acts as minimum width so the calculated real column width will never be smaller than this configured value.
   * This allows the table to be displayable on small screens. The user can still make the column smaller, though.
   *
   * Note if you want to have a very small column, you may have to adjust the minimum width as well.
   *
   * Default is 60.
   */
  width?: number;

  /**
   * Configures the minimum width of this column. With this value you can control how small the user can make the column.
   *
   * Default is {@link Column.DEFAULT_MIN_WIDTH}
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
   * Configures the header text of this column.
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
   * Comparator used for sorting.
   *
   * Default is {@link comparators.TEXT}
   */
  comparator?: ColumnComparator<string>;

  /**
   * Configures whether the column is displayable or not. A non-displayable column is always invisible for the user.
   *
   * This property sets the 'displayable' dimension for the {@link visible} property and therefore influences the computed state.
   *
   * Default is true.
   */
  displayable?: boolean;

  /**
   * Defines whether the column should be visible.
   *
   * Visible is a multidimensional property, which means, the column will only be visible if every dimension is true.
   *
   * If a boolean is passed, the value will be used for the 'default' dimension.
   * Alternatively, an object can be passed containing the dimensions. If a dimension is not set explicitly, it defaults to true.
   *
   * The available dimensions are:
   * - default: The default dimension.
   * - granted: Defines whether the column is allowed to be visible.
   * - displayable: Defines whether the column should ever be visible, can also be set by {@link displayable}.
   *
   * Default is true.
   */
  visible?: boolean | Record<string, boolean>;

  /**
   * Defines whether the column is allowed to be visible.
   *
   * This property sets the 'granted' dimension for the {@link visible} property and therefore influences the computed state.
   *
   * Default is true.
   */
  visibleGranted?: boolean;

  textBased?: boolean;

  /**
   * Configures, if the header menu is enabled for this column header.
   *
   * When header menu is disabled, a click on the header will toggle between ascending and descending sorting instead of opening the header popup.
   *
   * Default is true.
   */
  headerMenuEnabled?: boolean;

  [property: string]: any; // allow custom properties
}
