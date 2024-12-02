/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupCall, LookupEditor, LookupResult, ValueFieldEventMap} from '../../index';

export interface LookupEditorLookupCallDoneEvent<TValue = any, TSource extends LookupEditor<TValue> = LookupEditor<TValue>> extends Event<TSource> {
  result: LookupResult<TValue>;
}

export interface LookupEditorPrepareLookupCallEvent<TValue = any, TSource extends LookupEditor<TValue> = LookupEditor<TValue>> extends Event<TSource> {
  lookupCall: LookupCall<TValue>;
}

export interface LookupEditorEventMap<TValue> extends ValueFieldEventMap<TValue[]> {
  'lookupCallDone': LookupEditorLookupCallDoneEvent<TValue>;
  'prepareLookupCall': LookupEditorPrepareLookupCallEvent<TValue>;
}
