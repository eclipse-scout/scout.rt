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
import {Calendar, CalendarItem, DateRange, JsonDateRange, WidgetModel} from '../index';

export interface CalendarComponentModel extends WidgetModel {
  parent?: Calendar;
  fromDate?: string;
  toDate?: string;
  /**
   * Selected is a GUI only property (the model doesn't have it)
   */
  selected?: boolean;
  fullDay?: boolean;
  item?: CalendarItem;
  coveredDaysRange?: DateRange | JsonDateRange;
}
