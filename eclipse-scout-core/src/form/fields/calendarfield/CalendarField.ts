/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Device, FormField} from '../../../index';

export default class CalendarField extends FormField {

  constructor() {
    super();
    this.gridDataHints.weightY = 1;
    this._addWidgetProperties(['calendar']);
  }

  _render() {
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
