/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PopupEventMap, PropertyChangeEvent, Widget, WidgetPopup} from '../index';

export interface WidgetPopupMoveEvent<T = WidgetPopup> extends Event<T> {
  top: number;
  left: number;
}

export interface WidgetPopupEventMap extends PopupEventMap {
  'move': WidgetPopupMoveEvent;
  'propertyChange:closable': PropertyChangeEvent<boolean>;
  'propertyChange:content': PropertyChangeEvent<Widget>;
  'propertyChange:movable': PropertyChangeEvent<boolean>;
  'propertyChange:resizable': PropertyChangeEvent<boolean>;
}
