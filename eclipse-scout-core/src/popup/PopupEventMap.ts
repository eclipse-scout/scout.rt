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
