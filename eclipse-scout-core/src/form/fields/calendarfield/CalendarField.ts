/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarFieldModel, Device, FormField} from '../../../index';

export class CalendarField extends FormField implements CalendarFieldModel {
  declare model: CalendarFieldModel;

  calendar: Calendar;

  constructor() {
    super();
    this.gridDataHints.weightY = 1;
    this._addWidgetProperties(['calendar']);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'calendar-field');
    this.$container.toggleClass('mobile', Device.get().type === Device.Type.MOBILE);
    this.addLabel();
    this.addStatus();
    if (this.calendar) {
      this.calendar.render();
      this.addField(this.calendar.$container);
    }
  }
}
