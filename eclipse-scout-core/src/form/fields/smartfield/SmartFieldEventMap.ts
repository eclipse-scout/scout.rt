/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, LookupCall, LookupRow, PropertyChangeEvent, SmartField, Status, ValueFieldEventMap} from '../../../index';
import {SmartFieldActiveFilter, SmartFieldLookupResult} from './SmartField';
import {ValueFieldAcceptInputEvent} from '../ValueFieldEventMap';

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

export default interface SmartFieldEventMap<TValue> extends ValueFieldEventMap<TValue> {
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
