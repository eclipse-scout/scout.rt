/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, ObjectModel, ObjectModelWithId} from '../index';

export interface LookupRowModel<Key> extends ObjectModel<LookupRow<Key>>, ObjectModelWithId {
  key?: Key;
  text?: string;
  parentKey?: Key;
  enabled?: boolean;
  active?: boolean;
  additionalTableRowData?: Record<string, any>;
  cssClass?: string;
  iconId?: string;
  tooltipText?: string;
  backgroundColor?: string;
  foregroundColor?: string;
  font?: string;
}
