/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BusyIndicator, Event, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface BusyIndicatorEventMap extends WidgetEventMap {
  'cancel': Event<BusyIndicator>;
  'propertyChange:details': PropertyChangeEvent<string>;
  'propertyChange:label': PropertyChangeEvent<string>;
}
