/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, ColumnDescriptor, LookupCallOrModel, LookupRow, SmartFieldActiveFilter, SmartFieldDisplayStyle, ValueFieldModel} from '../../../index';

export interface SmartFieldModel<TValue> extends ValueFieldModel<TValue> {
  lookupCall?: LookupCallOrModel<TValue>;
  /**
   * CodeTypeId {@link CodeType.id} or CodeType ref. See {@link codes.get}.
   */
  codeType?: string | (new() => CodeType<any>);
  lookupRow?: LookupRow<TValue>;
  /**
   * Default is false.
   */
  browseHierarchy?: boolean;
  /**
   * A positive number, _not_ null or undefined!
   *
   * Default is SmartField.DEFAULT_BROWSE_MAX_COUNT.
   */
  browseMaxRowCount?: number;
  /**
   * Valid when browseHierarchy is true.
   *
   * Default is true.
   */
  browseAutoExpandAll?: boolean;
  /**
   * Valid when browseHierarchy is true.
   *
   * Default is false.
   */
  browseLoadIncremental?: boolean;
  /**
   * Configures whether this smartfield only shows proposals if a text search string has been entered.
   * Set this property to true if you expect a large amount of data for an unconstrained search.
   *
   * Default value is false.
   */
  searchRequired?: boolean;
  /**
   * true: inactive rows are display together with active rows
   * false: inactive rows ae only displayed when selected by the model
   *
   * Default is false.
   */
  activeFilterEnabled?: boolean;
  activeFilter?: SmartFieldActiveFilter;
  activeFilterLabels?: string[];
  /**
   * This property has only an effect when the smart field has a table proposal chooser.
   * When the returned value is <code>null</code>, the table proposal chooser has only one column (showing the lookup row text) without column header.
   * To change this default behavior, return an array of ColumnDescriptors.
   */
  columnDescriptors?: ColumnDescriptor[];
  /**
   * Default is SmartField.DisplayStyle.DEFAULT.
   */
  displayStyle?: SmartFieldDisplayStyle;
  touchMode?: boolean;
  embedded?: boolean;
  /**
   * Default is 500.
   */
  maxLength?: number;
}
