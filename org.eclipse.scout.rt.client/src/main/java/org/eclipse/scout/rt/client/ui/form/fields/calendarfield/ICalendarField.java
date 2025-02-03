/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.calendarfield;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface ICalendarField<T extends ICalendar> extends IValueField<Date> {

  T getCalendar();

  /**
   * reload all calendar items<br>
   * Convenience for {@link ICalendar#reloadCalendarItems()}
   */
  void reloadCalendarItems();
}
