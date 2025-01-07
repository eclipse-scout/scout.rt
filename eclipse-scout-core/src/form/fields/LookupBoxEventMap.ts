/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupBox, LookupCall, LookupResult, PropertyChangeEvent, Status, ValueFieldEventMap} from '../../index';

export interface LookupBoxLookupCallDoneEvent<TValue = any, T = LookupBox<TValue>> extends Event<T> {
  result: LookupResult<TValue>;
}

export interface LookupBoxPrepareLookupCallEvent<TValue = any, T = LookupBox<TValue>> extends Event<T> {
  lookupCall: LookupCall<TValue>;
}

export interface LookupBoxEventMap<TLookup, TValue = TLookup[]> extends ValueFieldEventMap<TValue> {
  'lookupCallDone': LookupBoxLookupCallDoneEvent<TLookup>;
  'prepareLookupCall': LookupBoxPrepareLookupCallEvent<TLookup>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<TLookup>>;
  'propertyChange:lookupStatus': PropertyChangeEvent<Status>;
}
