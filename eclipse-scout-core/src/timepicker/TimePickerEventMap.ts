/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, TimePicker, WidgetEventMap} from '../index';

export interface TimePickerTimeSelectEvent<T = TimePicker> extends Event<T> {
  time: Date;
}

export interface TimePickerEventMap extends WidgetEventMap {
  'timeSelect': TimePickerTimeSelectEvent;
  'propertyChange:preselectedTime': PropertyChangeEvent<Date>;
  'propertyChange:selectedTime': PropertyChangeEvent<Date>;
}
