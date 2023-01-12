/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormFieldEventMap, PropertyChangeEvent, ValueField, ValueFieldClearable, ValueFieldFormatter, ValueFieldParser, ValueFieldValidator} from '../../index';

export interface ValueFieldAcceptInputEvent<TValue = any, T = ValueField<TValue>> extends Event<T> {
  displayText: string;
  whileTyping: boolean;
}

export interface ValueFieldParseEvent<TValue = any, T = ValueField<TValue>> extends Event<T> {
  displayText: string;
}

export interface ValueFieldParseErrorEvent<TValue = any, T = ValueField<TValue>> extends Event<T> {
  displayText: string;
  error: any;
}

export interface ValueFieldEventMap<TValue> extends FormFieldEventMap {
  'acceptInput': ValueFieldAcceptInputEvent<TValue>;
  'clear': Event<ValueField<TValue>>;
  'parse': ValueFieldParseEvent<TValue>;
  'parseError': ValueFieldParseErrorEvent<TValue>;
  'propertyChange:value': PropertyChangeEvent<TValue>;
  'propertyChange:clearable': PropertyChangeEvent<ValueFieldClearable>;
  'propertyChange:displayText': PropertyChangeEvent<string>;
  'propertyChange:formatter': PropertyChangeEvent<ValueFieldFormatter<TValue>>;
  'propertyChange:hasText': PropertyChangeEvent<boolean>;
  'propertyChange:parser': PropertyChangeEvent<ValueFieldParser<TValue>>;
  'propertyChange:validators': PropertyChangeEvent<ValueFieldValidator<TValue>[]>;
}
