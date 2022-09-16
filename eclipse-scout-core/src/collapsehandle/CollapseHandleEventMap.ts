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
import {CollapseHandle, Event, PropertyChangeEvent, WidgetEventMap} from '../index';
import {CollapseHandleHorizontalAlignment} from './CollapseHandle';

export interface CollapseHandleActionEvent<T extends CollapseHandle = CollapseHandle> extends Event<T> {
  left?: boolean;
  right?: boolean;
}

export default interface CollapseHandleEventMap extends WidgetEventMap {
  'action': CollapseHandleActionEvent;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<CollapseHandleHorizontalAlignment>;
  'propertyChange:leftVisible': PropertyChangeEvent<boolean>;
  'propertyChange:rightVisible': PropertyChangeEvent<boolean>;

}
