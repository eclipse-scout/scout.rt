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
import {Popup, ValueFieldModel} from '../../../index';

export default interface DateFieldModel extends ValueFieldModel<Date> {
  popup?: Popup;
  touchMode?: boolean;
  /**
   * Configure the time picker steps. E.g. 15 for 15 minute steps starting with every full hour.
   * If 60 % resolution != 0, the minute steps starts every hour with 00 and rise in resolution steps.
   */
  timePickerResolution?: number;
  embedded?: boolean;
  /**
   * Date to be used when setting a value "automatically", e.g. when the date picker is opened initially or when a date or time is entered and the other component has to be filled.
   * If no auto date is set (which is the default), the current date (with time part "00:00:00.000") is used.
   */
  autoDate?: Date | string;
  /**
   * Sets a list of allowed dates.
   * If the given array contains elements, the dates contained in the list can be chosen in the date-picker or entered manually in the date-field.
   * All other dates are disabled.
   * If the list is empty or null, all dates are available again.
   */
  allowedDates?: string[] | Date[];

  hasDate?: boolean;
  dateHasText?: boolean;
  dateFocused?: boolean;
  dateFormatPattern?: string;

  hasTime?: boolean;
  timeHasText?: boolean;
  timeFocused?: boolean;
  timeFormatPattern?: string;
}
