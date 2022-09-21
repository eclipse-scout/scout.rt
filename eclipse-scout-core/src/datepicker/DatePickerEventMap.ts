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
import {DateFormat, DatePicker, Event, PropertyChangeEvent, WidgetEventMap} from '../index';
import {DatePickerMonth} from './DatePicker';

export interface DatePickerDateSelectEvent<D extends DatePicker = DatePicker> extends Event<D> {
  date: Date;
}

export default interface DatePickerEventMap extends WidgetEventMap {
  'dateSelect': DatePickerDateSelectEvent;
  'propertyChange:dateFormat': PropertyChangeEvent<DateFormat>;
  'propertyChange:months': PropertyChangeEvent<DatePickerMonth[]>;
  'propertyChange:preselectedDate': PropertyChangeEvent<Date>;
  'propertyChange:selectedDate': PropertyChangeEvent<Date>;
  'propertyChange:viewDate': PropertyChangeEvent<Date>;
}
