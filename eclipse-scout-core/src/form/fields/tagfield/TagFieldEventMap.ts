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
