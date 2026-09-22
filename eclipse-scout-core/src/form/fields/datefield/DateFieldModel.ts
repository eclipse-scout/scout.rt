/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AllowedDateProvider, ValueFieldModel} from '../../../index';

export interface DateFieldModel extends ValueFieldModel<Date, Date | string> {
  /**
   * Configures the time picker steps. E.g. 15 for 15 minute steps starting with every full hour.
   *
   * If 60 % resolution != 0, the minute steps starts every hour with 00 and rise in resolution steps.
   *
   * Default is 30 minutes.
   */
  timePickerResolution?: number;
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
   * If {@link allowedDateProvider} is set, this value will be ignored;
   */
  allowedDates?: (string | Date)[];
  /**
   * Provides allowed dates to be chosen in the date-picker or entered manually in the date-field. If no {@link AllowedDateProvider} is set {@link allowedDates} are used.
   */
  allowedDateProvider?: AllowedDateProvider;
  /**
   * Configures whether a field to enter a date should be shown.
   *
   * Default is true.
   */
  hasDate?: boolean;
  dateHasText?: boolean;
  dateFocused?: boolean;
  dateFormatPattern?: string;
  /**
   * Configures whether a field to enter a time should be shown.
   *
   * Default is false.
   */
  hasTime?: boolean;
  timeHasText?: boolean;
  timeFocused?: boolean;
  timeFormatPattern?: string;
  touchMode?: boolean;
}
