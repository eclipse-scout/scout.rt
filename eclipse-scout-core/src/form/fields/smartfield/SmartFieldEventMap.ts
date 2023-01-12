/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupCall, LookupRow, PropertyChangeEvent, SmartField, SmartFieldActiveFilter, SmartFieldLookupResult, Status, ValueFieldAcceptInputEvent, ValueFieldEventMap} from '../../../index';

export interface SmartFieldAcceptByTextEvent<TValue = any, T = SmartField<TValue>> extends Event<T> {
  searchText: string;
  errorStatus: Status;
}

export interface SmartFieldAcceptInputEvent<TValue = any, T = SmartField<TValue>> extends ValueFieldAcceptInputEvent<TValue, T> {
  displayText: string;
  errorStatus: Status;
  value: TValue;
  lookupRow: LookupRow<TValue>;
  acceptByLookupRow: boolean;
  failure: boolean;
}

export interface SmartFieldLookupCallDoneEvent<TValue = any, T = SmartField<TValue>> extends Event<T> {
  result: SmartFieldLookupResult<TValue>;
}

export interface SmartFieldPrepareLookupCallEvent<TValue = any, T = SmartField<TValue>> extends Event<T> {
  lookupCall: LookupCall<TValue>;
}

export interface SmartFieldEventMap<TValue> extends ValueFieldEventMap<TValue> {
  'acceptByText': SmartFieldAcceptByTextEvent<TValue>;
  'acceptInput': SmartFieldAcceptInputEvent<TValue>;
  'lookupCallDone': SmartFieldLookupCallDoneEvent<TValue>;
  'prepareLookupCall': SmartFieldPrepareLookupCallEvent<TValue>;
  'propertyChange:activeFilter': PropertyChangeEvent<SmartFieldActiveFilter>;
  'propertyChange:activeFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:browseAutoExpandAll': PropertyChangeEvent<boolean>;
  'propertyChange:browseLoadIncremental': PropertyChangeEvent<boolean>;
  'propertyChange:browseMaxRowCount': PropertyChangeEvent<number>;
  'propertyChange:codeType': PropertyChangeEvent<string>;
  'propertyChange:initActiveFilter': PropertyChangeEvent<SmartFieldActiveFilter>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<TValue>>;
  'propertyChange:lookupRow': PropertyChangeEvent<LookupRow<TValue>>;
  'propertyChange:lookupStatus': PropertyChangeEvent<Status>;
  'propertyChange:maxLength': PropertyChangeEvent<number>;
  'propertyChange:searchRequired': PropertyChangeEvent<boolean>;
}
