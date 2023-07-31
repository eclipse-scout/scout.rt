/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, Switch, SwitchDisplayStyle, WidgetEventMap} from '../index';

export interface SwitchSwitchEvent<TSource = Switch> extends Event<TSource> {
  originalEvent: JQuery.Event;
  oldValue: boolean;
  newValue: boolean;
}

export interface SwitchEventMap extends WidgetEventMap {
  'switch': SwitchSwitchEvent;
  'propertyChange:activated': PropertyChangeEvent<boolean>;
  'propertyChange:label': PropertyChangeEvent<string>;
  'propertyChange:labelHtmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:labelVisible': PropertyChangeEvent<boolean>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
  'propertyChange:iconVisible': PropertyChangeEvent<boolean>;
  'propertyChange:displayStyle': PropertyChangeEvent<SwitchDisplayStyle>;
  'propertyChange:tabbable': PropertyChangeEvent<boolean>;
}
