/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {codes, CodeType, ColumnModel, LookupCallOrModel} from '../../index';

export interface LookupCallColumnModel<TValue, TKey = TValue> extends ColumnModel<TValue> {
  /**
   * Configures the {@link LookupCall} that is used to resolve the values and to load the proposals if the column is editable.
   */
  lookupCall?: LookupCallOrModel<TKey>;
  /**
   * If set, a {@link CodeLookupCall} is created and used for the property {@link lookupCall}.
   *
   * The property accepts a {@link CodeType.id} (see {@link codes.get}).
   */
  codeType?: string;
  /**
   * Configures the {@link SmartFieldModel.browseHierarchy} of the cell editor if the column is editable.
   * Does not have an effect otherwise.
   */
  browseHierarchy?: boolean;
  /**
   * Configures the {@link SmartFieldModel.browseMaxRowCount} of the cell editor if the column is editable.
   * Does not have an effect otherwise.
   */
  browseMaxRowCount?: number;
}
