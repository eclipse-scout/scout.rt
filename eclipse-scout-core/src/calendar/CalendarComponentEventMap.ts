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
import {CalendarComponent, Event, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface CalendarComponentAppLinkActionEvent<T = CalendarComponent> extends Event<T> {
  ref: string;
}


export interface CalendarComponentEventMap extends WidgetEventMap {
  'appLinkAction': CalendarComponentAppLinkActionEvent;
  'propertyChange:selected': PropertyChangeEvent<boolean>;
}
