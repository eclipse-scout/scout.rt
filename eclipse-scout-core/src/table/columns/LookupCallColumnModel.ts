/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, ColumnModel, LookupCallOrModel} from '../../index';

export interface LookupCallColumnModel<TValue, TKey = TValue> extends ColumnModel<TValue> {
  /**
   * Configures the {@link LookupCall} that is used to format the values.
   *
   * The property will also be passed to the cell editor if the column is {@link editable}.
   *
   * Default is null.
   */
  lookupCall?: LookupCallOrModel<TKey>;
  /**
   * If set, a {@link CodeLookupCall} is created and used for the property {@link lookupCall}.
   *
   * The property accepts a {@link CodeType} class or a {@link CodeType.id} (see {@link CodeTypeCache.get}).
   *
   * It will also be passed to the cell editor if the column is {@link editable}.
   *
   * Default is null.
   */
  codeType?: string | (new() => CodeType<TKey>);
  /**
   * Configures the {@link SmartFieldModel.browseHierarchy} of the cell editor if the column is {@link editable}.
   * Does not have an effect otherwise.
   *
   * Default is false.
   */
  browseHierarchy?: boolean;
  /**
   * Configures the {@link SmartFieldModel.browseMaxRowCount} of the cell editor if the column is {@link editable}.
   * Does not have an effect otherwise.
   *
   * Default is {@link SmartField.DEFAULT_BROWSE_MAX_COUNT}.
   */
  browseMaxRowCount?: number;
}
