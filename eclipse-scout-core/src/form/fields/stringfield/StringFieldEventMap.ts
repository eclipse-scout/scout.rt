/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BasicFieldEventMap, Event, PropertyChangeEvent, StringField, StringFieldFormat} from '../../../index';

export interface StringFieldSelectionChangeEvent<TStringField = StringField> extends Event<TStringField> {
  selectionStart: number;
  selectionEnd: number;
}

export interface StringFieldEventMap extends BasicFieldEventMap<string> {
  'action': Event<StringField>;
  'selectionChange': StringFieldSelectionChangeEvent;
  'propertyChange:format': PropertyChangeEvent<StringFieldFormat>;
  'propertyChange:hasAction': PropertyChangeEvent<boolean>;
  'propertyChange:inputMasked': PropertyChangeEvent<boolean>;
  'propertyChange:maxLength': PropertyChangeEvent<number>;
  'propertyChange:multilineText': PropertyChangeEvent<boolean>;
  'propertyChange:selectionEnd': PropertyChangeEvent<number>;
  'propertyChange:selectionStart': PropertyChangeEvent<number>;
  'propertyChange:selectionTrackingEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:spellCheckEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:trimText': PropertyChangeEvent<boolean>;
  'propertyChange:wrapText': PropertyChangeEvent<boolean>;
}
