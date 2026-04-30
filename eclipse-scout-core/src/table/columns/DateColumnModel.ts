/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnModel, DateFormat, DateGroupType} from '../../index';

export interface DateColumnModel extends ColumnModel<Date> {
  /**
   * Specifies the {@link DateFormat} to be used when formatting cell values.
   *
   * If omitted, the column creates a default format based on the current locale and {@link hasDate}/{@link hasTime}.
   * When a string is supplied, it is treated as a pattern for a new {@link DateFormat} instance.
   */
  format?: DateFormat | string;
  /**
   * Specifies a custom format that can be used to group the cell values in this column. All values
   * with the same formatted representation are grouped together, sorted by the underlying date value.
   *
   * This format only has an effect if {@link groupType} is not set explicitly. It does not affect the
   * display of the individual cell values, which are always formatted using {@link format}.
   *
   * Typically, this only needs to be set if the desired format cannot be provided by one of the
   * {@link DateGroupType} values.
   *
   * Default is `'yyyy'` (year).
   */
  groupFormat?: DateFormat | string;
  /**
   * Specifies how the values in this column are grouped when grouping is enabled ({@link grouped}).
   * Groups are sorted naturally, e.g. months from January to December. If this property is `null`,
   * the data is grouped by {@link groupFormat}.
   *
   * Supported values are defined in the enum {@link DateGroupType}. The user can always change the
   * group type via the table header menu of this column.
   *
   * Default is `null`.
   */
  groupType?: DateGroupType;
  /**
   * Configures whether the values of this column should show the date.
   *
   * If {@link format} is set, this configuration has no effect.
   *
   * The property will also be passed to the cell editor if the column is {@link editable}.
   *
   * Default is true.
   */
  hasDate?: boolean;
  /**
   * Configures whether the values of this column should show the time.
   *
   * If {@link format} is set, this configuration has no effect.
   *
   * The property will also be passed to the cell editor if the column is {@link editable}.
   *
   * Default is false.
   */
  hasTime?: boolean;
}
