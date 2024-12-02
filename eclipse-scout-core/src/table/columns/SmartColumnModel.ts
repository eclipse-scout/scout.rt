/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCallColumnModel} from '../../index';

export interface SmartColumnModel<TValue> extends LookupCallColumnModel<TValue> {
  /**
   * Configures the {@link SmartFieldModel.browseAutoExpandAll} of the cell editor if the column is editable.
   * Does not have an effect otherwise.
   */
  browseAutoExpandAll?: boolean;
  /**
   * Configures the {@link SmartFieldModel.browseLoadIncremental} of the cell editor if the column is editable.
   * Does not have an effect otherwise.
   */
  browseLoadIncremental?: boolean;
  /**
   * Configures the {@link SmartFieldModel.activeFilterEnabled} of the cell editor if the column is editable.
   * Does not have an effect otherwise.
   */
  activeFilterEnabled?: boolean;
}
