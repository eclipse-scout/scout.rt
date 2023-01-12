/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, Switch, WidgetEventMap} from '../index';

export interface SwitchEventMap extends WidgetEventMap {
  'switch': Event<Switch>;
  'propertyChange:activated': PropertyChangeEvent<boolean>;
  'propertyChange:label': PropertyChangeEvent<string>;
  'propertyChange:tooltipText': PropertyChangeEvent<string>;
}
