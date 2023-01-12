/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Popup, PopupAlignment, PropertyChangeEvent, Widget, WidgetEventMap} from '../index';

export interface PopupEventMap extends WidgetEventMap {
  'close': Event<Popup>;
  'locationChange': Event<Popup>;
  'propertyChange:$anchor': PropertyChangeEvent<JQuery>;
  'propertyChange:anchor': PropertyChangeEvent<Widget>;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<PopupAlignment>;
  'propertyChange:horizontalSwitch': PropertyChangeEvent<boolean>;
  'propertyChange:trimHeight': PropertyChangeEvent<boolean>;
  'propertyChange:trimWidth': PropertyChangeEvent<boolean>;
  'propertyChange:verticalAlignment': PropertyChangeEvent<PopupAlignment>;
  'propertyChange:verticalSwitch': PropertyChangeEvent<boolean>;
  'propertyChange:withArrow': PropertyChangeEvent<boolean>;
}
