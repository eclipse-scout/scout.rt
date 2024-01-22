/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, ColumnModel, LookupCallOrModel} from '../../index';

export interface SmartColumnModel<TValue> extends ColumnModel<TValue> {
  /**
   * CodeTypeId {@link CodeType.id} or CodeType ref. See {@link codes.get}.
   */
  codeType?: string | (new() => CodeType<any>);
  lookupCall?: LookupCallOrModel<TValue>;
  browseHierarchy?: boolean;
  browseMaxRowCount?: number;
  browseAutoExpandAll?: boolean;
  browseLoadIncremental?: boolean;
  activeFilterEnabled?: boolean;
}
