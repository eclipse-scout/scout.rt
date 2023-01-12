/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, ColumnUserFilterValues, TableUserFilterModel} from '../../index';

export interface ColumnUserFilterModel extends TableUserFilterModel {
  column?: Column<any> | string;

  /**
   * This property is used to check early whether this filter can produce filter-fields.
   * Set this property to true in your subclass, if it creates filter fields.
   */
  hasFilterFields?: boolean;

  /**
   * array of (normalized) key, text composite
   */
  availableValues?: ColumnUserFilterValues[];

  /**
   * array of (normalized) keys
   */
  selectedValues?: (string | number)[];
}
