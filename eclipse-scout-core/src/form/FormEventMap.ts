/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayHint, DisplayParent, DisplayViewId, Event, Form, FormValidator, GroupBox, PropertyChangeEvent, Status, ValidationResult, WidgetEventMap} from '../index';

export interface FormRevealInvalidFieldEvent<F extends Form = Form> extends Event<F> {
  validationResult: ValidationResult;
}

export interface FormMoveEvent<F extends Form = Form> extends Event<F> {
  left: number;
  top: number;
}

export interface FormErrorEvent<F extends Form = Form> extends Event<F> {
  phase: 'load' | 'save' | 'postLoad';
  /**
   * Typically an Error but may be anything as everything can be thrown in JavaScript.
   */
  error: any;
}

export interface FormInvalidEvent<F extends Form = Form> extends Event<F> {
  status: Status;
}

export interface FormEventMap extends WidgetEventMap {
  'abort': Event<Form>;
  'close': Event<Form>;
  'load': Event<Form>;
  'move': FormMoveEvent;
  'postLoad': Event<Form>;
  'reset': Event<Form>;
  'error': FormErrorEvent;
  'invalid': FormInvalidEvent;
  'revealInvalidField': FormRevealInvalidFieldEvent;
  'save': Event<Form>;
  'propertyChange:askIfNeedSave': PropertyChangeEvent<boolean>;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:data': PropertyChangeEvent<any>;
  'propertyChange:displayHint': PropertyChangeEvent<DisplayHint>;
  'propertyChange:displayParent': PropertyChangeEvent<DisplayParent>;
  'propertyChange:displayViewId': PropertyChangeEvent<DisplayViewId>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:maximized': PropertyChangeEvent<boolean>;
  'propertyChange:modal': PropertyChangeEvent<boolean>;
  'propertyChange:movable': PropertyChangeEvent<boolean>;
  'propertyChange:resizable': PropertyChangeEvent<boolean>;
  'propertyChange:rootGroupBox': PropertyChangeEvent<GroupBox>;
  'propertyChange:saveNeeded': PropertyChangeEvent<boolean>;
  'propertyChange:saveNeededVisible': PropertyChangeEvent<boolean>;
  'propertyChange:showOnOpen': PropertyChangeEvent<boolean>;
  'propertyChange:status': PropertyChangeEvent<Status>;
  'propertyChange:subTitle': PropertyChangeEvent<string>;
  'propertyChange:title': PropertyChangeEvent<string>;
  'propertyChange:validators': PropertyChangeEvent<FormValidator[]>;
  'propertyChange:views': PropertyChangeEvent<Form[]>;
}
