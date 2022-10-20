/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Calendar, CalendarComponent} from '../../index';

/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class CalendarSpecHelper {
  constructor(session) {
    this.session = session;
  }

  createCalendar(model) {
    let calendar = new Calendar();
    calendar.init(model);
    return calendar;
  }

  createSimpleModel() {
    let c = createSimpleModel('Calendar', this.session);
    c.selectedDate = '2016-07-20 14:09:28.556';
    c.startHour = 6;
    c.displayMode = Calendar.DisplayMode.DAY;
    return c;
  }

  createComponent(options, cal) {
    let component = new CalendarComponent();
    let model = this.createComponentModel(options);
    component.init(model);
    component.parent = cal;
    return component;
  }

  createComponentModel(options) {
    let c = createSimpleModel('CalendarComponent', this.session);
    for (let prop in options) {
      c[prop] = options[prop];
    }
    return c;
  }
}
