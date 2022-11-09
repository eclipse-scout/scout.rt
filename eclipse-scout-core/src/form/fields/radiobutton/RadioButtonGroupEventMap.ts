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

import {Event, FormField, LogicalGridLayoutConfig, LookupCall, LookupResult, PropertyChangeEvent, RadioButton, RadioButtonGroup, Status, ValueFieldEventMap} from '../../../index';

export interface RadioButtonGroupLookupCallDoneEvent<TValue, T = RadioButtonGroup<TValue>> extends Event<T> {
  result: LookupResult<TValue>;
}

export interface RadioButtonGroupPrepareLookupCallEvent<TValue, T = RadioButtonGroup<TValue>> extends Event<T> {
  lookupCall: LookupCall<TValue>;
}

export interface RadioButtonGroupEventMap<TValue> extends ValueFieldEventMap<TValue> {
  'lookupCallDone': RadioButtonGroupLookupCallDoneEvent<TValue>;
  'prepareLookupCall': RadioButtonGroupPrepareLookupCallEvent<TValue>;
  'propertyChange:fields': PropertyChangeEvent<FormField[]>;
  'propertyChange:gridColumnCount': PropertyChangeEvent<number>;
  'propertyChange:layoutConfig': PropertyChangeEvent<LogicalGridLayoutConfig>;
  'propertyChange:lookupCall': PropertyChangeEvent<LookupCall<TValue>>;
  'propertyChange:lookupStatus': PropertyChangeEvent<Status>;
  'propertyChange:selectedButton': PropertyChangeEvent<RadioButton<TValue>>;
}
