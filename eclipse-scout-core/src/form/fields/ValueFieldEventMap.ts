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
import {Event, FormFieldEventMap, PropertyChangeEvent, ValueField} from '../../index';
import {ValueFieldClearable, ValueFieldFormatter, ValueFieldParser, ValueFieldValidator} from './ValueField';


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

export default interface ValueFieldEventMap<TValue> extends FormFieldEventMap {
  'acceptInput': ValueFieldAcceptInputEvent<TValue>;
  'clear': Event<ValueField<TValue>>;
  'parse': ValueFieldParseEvent<TValue>;
  'parseError': ValueFieldParseErrorEvent<TValue>;
  'propertyChange:clearable': PropertyChangeEvent<ValueFieldClearable>;
  'propertyChange:displayText': PropertyChangeEvent<string>;
  'propertyChange:formatter': PropertyChangeEvent<ValueFieldFormatter<TValue>>;
  'propertyChange:hasText': PropertyChangeEvent<boolean>;
  'propertyChange:parser': PropertyChangeEvent<ValueFieldParser<TValue>>;
  'propertyChange:validators': PropertyChangeEvent<ValueFieldValidator<TValue>[]>;
}
