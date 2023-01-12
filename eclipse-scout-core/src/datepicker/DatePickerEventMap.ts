/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateFormat, DatePicker, DatePickerMonth, Event, PropertyChangeEvent, WidgetEventMap} from '../index';

export interface DatePickerDateSelectEvent<D extends DatePicker = DatePicker> extends Event<D> {
  date: Date;
}

export interface DatePickerEventMap extends WidgetEventMap {
  'dateSelect': DatePickerDateSelectEvent;
  'propertyChange:dateFormat': PropertyChangeEvent<DateFormat>;
  'propertyChange:months': PropertyChangeEvent<DatePickerMonth[]>;
  'propertyChange:preselectedDate': PropertyChangeEvent<Date>;
  'propertyChange:selectedDate': PropertyChangeEvent<Date>;
  'propertyChange:viewDate': PropertyChangeEvent<Date>;
}
