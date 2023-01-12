/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnEventMap, Event, LookupCall, LookupResult, PropertyChangeEvent, SmartColumn, TableRow} from '../../index';

export interface SmartColumnCallDoneEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends Event<S> {
  result: LookupResult<TValue>;
}

export interface SmartColumnPrepareLookupCallEvent<TValue = any, S extends SmartColumn<TValue> = SmartColumn<TValue>> extends Event<S> {
  lookupCall: LookupCall<TValue>;
  row?: TableRow;
}

export interface SmartColumnEventMap<TValue> extends ColumnEventMap {
  'lookupCallDone': SmartColumnCallDoneEvent<TValue>;
  'prepareLookupCall': SmartColumnPrepareLookupCallEvent<TValue>;
  'propertyChange:activeFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:browseAutoExpandAll': PropertyChangeEvent<boolean>;
  'propertyChange:browseHierarchy': PropertyChangeEvent<boolean>;
  'propertyChange:browseLoadIncremental': PropertyChangeEvent<boolean>;
  'propertyChange:browseMaxRowCount': PropertyChangeEvent<number>;
  'propertyChange:codeType': PropertyChangeEvent<string>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<any>>;
}
