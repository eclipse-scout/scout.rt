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
  format?: DateFormat | string;
  groupFormat?: DateFormat | string;
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
