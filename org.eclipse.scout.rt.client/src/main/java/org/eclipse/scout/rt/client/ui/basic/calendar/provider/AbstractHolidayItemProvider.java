/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar.provider;

import java.util.Date;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.shared.services.common.calendar.HolidayItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.shared.services.common.calendar.IHolidayCalendarService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

/**
 * Produces holiday calendar items
 */
public abstract class AbstractHolidayItemProvider extends AbstractCalendarItemProvider {

  @Override
  protected boolean getConfiguredMoveItemEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  protected String getConfiguredRemoteFile() {
    return "calendar/holidays.xml";
  }

  @Override
  protected void execLoadItemsInBackground(IClientSession session, Date minDate, Date maxDate, final Set<ICalendarItem> result) {
    IHolidayCalendarService service = BEANS.get(IHolidayCalendarService.class);
    if (service != null) {
      RemoteFile f = new RemoteFile(getConfiguredRemoteFile(), 0);
      result.addAll(service.getItems(f, minDate, maxDate));
    }
  }

  @Override
  protected void execDecorateCell(Cell cell, ICalendarItem item) {
    HolidayItem h = (HolidayItem) item;
    cell.setCssClass(h.getCssClass());
  }

}
