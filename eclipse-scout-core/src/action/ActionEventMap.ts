/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, ActionStyle, ActionTextPosition, Alignment, Event, PropertyChangeEvent, TooltipPosition, WidgetEventMap} from '../index';

export interface ActionEventMap extends WidgetEventMap {
  'action': Event<Action>;
  'propertyChange:actionStyle': PropertyChangeEvent<ActionStyle>;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<Alignment>;
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
  'propertyChange:tooltipPosition': PropertyChangeEvent<TooltipPosition>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
}
