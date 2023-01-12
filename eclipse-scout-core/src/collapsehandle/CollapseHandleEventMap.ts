/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CollapseHandle, CollapseHandleHorizontalAlignment, Event, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface CollapseHandleActionEvent<T = CollapseHandle> extends Event<T> {
  left?: boolean;
  right?: boolean;
}

export interface CollapseHandleEventMap extends WidgetEventMap {
  'action': CollapseHandleActionEvent;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<CollapseHandleHorizontalAlignment>;
  'propertyChange:leftVisible': PropertyChangeEvent<boolean>;
  'propertyChange:rightVisible': PropertyChangeEvent<boolean>;
}
