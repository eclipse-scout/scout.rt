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
import {Action, Event, PropertyChangeEvent, WidgetEventMap} from '../index';
import {ActionStyle, ActionTextPosition} from './Action';

export default interface ActionEventMap extends WidgetEventMap {
  'action': Event<Action>;
  'propertyChange:actionStyle': PropertyChangeEvent<ActionStyle>;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<-1 | 0 | 1>;
  'propertyChange:htmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:keyStroke': PropertyChangeEvent<string>;
  'propertyChange:preventDoubleClick': PropertyChangeEvent<boolean>;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
  'propertyChange:tabbable': PropertyChangeEvent<boolean>;
  'propertyChange:text': PropertyChangeEvent<string>;
  'propertyChange:textPosition': PropertyChangeEvent<ActionTextPosition>;
  'propertyChange:textVisible': PropertyChangeEvent<boolean>;
  'propertyChange:toggleAction': PropertyChangeEvent<boolean>;
  'propertyChange:tooltipPosition': PropertyChangeEvent<'top' | 'bottom'>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
}
