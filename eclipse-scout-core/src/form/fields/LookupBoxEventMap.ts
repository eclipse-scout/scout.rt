/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

export interface LookupBoxEventMap<TValue> extends ValueFieldEventMap<TValue[]> {
  'lookupCallDone': LookupBoxLookupCallDoneEvent<TValue>;
  'prepareLookupCall': LookupBoxPrepareLookupCallEvent<TValue>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<TValue>>;
  'propertyChange:lookupStatus': PropertyChangeEvent<Status>;
}
