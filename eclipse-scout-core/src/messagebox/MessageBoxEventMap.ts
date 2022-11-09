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
import {DisplayParent, Event, MessageBox, MessageBoxOption, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface MessageBoxActionEvent<T = MessageBox> extends Event<T> {
  option: MessageBoxOption;
}

export interface MessageBoxEventMap extends WidgetEventMap {
  'action': MessageBoxActionEvent;
  'propertyChange:displayParent': PropertyChangeEvent<DisplayParent>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
}
