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
