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
import {DateFormat, WidgetModel} from '../index';

export interface DatePickerModel extends WidgetModel {
  /**
   * Preselected date can only be set if selectedDate is null. The preselected date is rendered differently, but
   * has no function otherwise. (It is used to indicate the day that will be selected when the user presses
   * the UP or DOWN key while no date is selected.)
   */
  preselectedDate?: Date;
  selectedDate?: Date;
  dateFormat?: DateFormat | string;
  viewDate?: Date;
  allowedDates?: Date[];
  touch?: boolean;
}
