/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupCall, LookupResult, PropertyChangeEvent, TagField, ValueFieldAcceptInputEvent, ValueFieldEventMap} from '../../../index';

export interface TagFieldAcceptInputEvent<T = TagField> extends ValueFieldAcceptInputEvent<string[], T> {
  value: string[];
}

export interface TagFieldLookupCallDoneEvent<T = TagField> extends Event<T> {
  result: LookupResult<string>;
}

export interface TagFieldPrepareLookupCallEvent<T = TagField> extends Event<T> {
  lookupCall: LookupCall<string>;
}

export interface TagFieldEventMap extends ValueFieldEventMap<string[]> {
  'acceptInput': TagFieldAcceptInputEvent;
  'lookupCallDone': TagFieldLookupCallDoneEvent;
  'prepareLookupCall': TagFieldPrepareLookupCallEvent;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<string>>;
  'propertyChange:maxLength': PropertyChangeEvent<number>;
}
