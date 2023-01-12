/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.Date;
import java.util.Set;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

@TunnelToServer
public interface IHolidayCalendarService extends ICalendarService {

  /**
   * default value: calendar/holidays.xml
   */
  Set<? extends ICalendarItem> getItems(RemoteFile f, Date minDate, Date maxDate);
}
