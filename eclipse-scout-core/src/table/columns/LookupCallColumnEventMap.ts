/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnEventMap, Event, LookupCall, LookupCallColumn, LookupResult, PropertyChangeEvent, TableRow} from '../../index';

export interface LookupCallColumnLookupCallDoneEvent<TValue = any, TKey = TValue, TSource extends LookupCallColumn<TValue, TKey> = LookupCallColumn<TValue, TKey>> extends Event<TSource> {
  result: LookupResult<TKey>;
}

export interface LookupCallColumnPrepareLookupCallEvent<TValue = any, TKey = TValue, TSource extends LookupCallColumn<TValue, TKey> = LookupCallColumn<TValue, TKey>> extends Event<TSource> {
  lookupCall: LookupCall<TKey>;
  row?: TableRow;
}

export interface LookupCallColumnEventMap<TValue, TKey = TValue> extends ColumnEventMap {
  'lookupCallDone': LookupCallColumnLookupCallDoneEvent<TValue, TKey>;
  'prepareLookupCall': LookupCallColumnPrepareLookupCallEvent<TValue, TKey>;
  'propertyChange:browseHierarchy': PropertyChangeEvent<boolean>;
  'propertyChange:browseMaxRowCount': PropertyChangeEvent<number>;
  'propertyChange:codeType': PropertyChangeEvent<string>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<TKey>>;
}
