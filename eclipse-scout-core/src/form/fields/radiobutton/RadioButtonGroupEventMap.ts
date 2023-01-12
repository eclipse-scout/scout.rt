/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
