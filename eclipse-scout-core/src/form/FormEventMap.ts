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
import {DisplayParent, Event, Form, GroupBox, PropertyChangeEvent, Status, WidgetEventMap} from '../index';
import {DisplayHint, ValidationResult} from './Form';

export interface FormRevealInvalidFieldEvent<F extends Form = Form> extends Event<F> {
  validationResult: ValidationResult;
}

export interface FormMoveEvent<F extends Form = Form> extends Event<F> {
  left: number;
  top: number;
}

export default interface FormEventMap extends WidgetEventMap {
  'abort': Event<Form>;
  'close': Event<Form>;
  'load': Event<Form>;
  'move': FormMoveEvent;
  'postLoad': Event<Form>;
  'reset': Event<Form>;
  'revealInvalidField': FormRevealInvalidFieldEvent;
  'save': Event<Form>;
  'propertyChange:askIfNeedSave': PropertyChangeEvent<boolean>;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:data': PropertyChangeEvent<object>;
  'propertyChange:displayHint': PropertyChangeEvent<DisplayHint>;
  'propertyChange:displayParent': PropertyChangeEvent<DisplayParent>;
  'propertyChange:displayViewId': PropertyChangeEvent<string>;
  'propertyChange:headerVisible': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:maximized': PropertyChangeEvent<boolean>;
  'propertyChange:modal': PropertyChangeEvent<boolean>;
  'propertyChange:movable': PropertyChangeEvent<boolean>;
  'propertyChange:resizable': PropertyChangeEvent<boolean>;
  'propertyChange:rootGroupBox': PropertyChangeEvent<GroupBox>;
  'propertyChange:saveNeededVisible': PropertyChangeEvent<boolean>;
  'propertyChange:showOnOpen': PropertyChangeEvent<boolean>;
  'propertyChange:status': PropertyChangeEvent<Status>;
  'propertyChange:subTitle': PropertyChangeEvent<string>;
  'propertyChange:title': PropertyChangeEvent<string>;
  'propertyChange:views': PropertyChangeEvent<Form[]>;
}
