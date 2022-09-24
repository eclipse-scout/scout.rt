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
import {Event, PropertyChangeEvent, TimePicker, WidgetEventMap} from '../index';

export interface TimePickerTimeSelectEvent<T extends TimePicker = TimePicker> extends Event<T> {
  time: Date;
}

export default interface TimePickerEventMap extends WidgetEventMap {
  'timeSelect': TimePickerTimeSelectEvent;
  'propertyChange:preselectedTime': PropertyChangeEvent<Date, TimePicker>;
  'propertyChange:selectedTime': PropertyChangeEvent<Date, TimePicker>;
}
