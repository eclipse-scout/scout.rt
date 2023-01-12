/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonDateRange;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.json.JSONObject;

public class JsonCalendarComponent<CALENDAR_COMPONENT extends CalendarComponent> extends AbstractJsonAdapter<CALENDAR_COMPONENT> {

  public JsonCalendarComponent(CALENDAR_COMPONENT model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CalendarComponent";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("item", new JsonCalendarItem(getModel().getItem()).toJson());
    json.put("fromDate", new JsonDate(getModel().getFromDate()).asJsonString());
    json.put("toDate", new JsonDate(getModel().getToDate()).asJsonString());
    if (getModel().getCoveredDaysRange() != null) {
      Object coveredDaysRange = new JsonDateRange(getModel().getCoveredDaysRange()).toJson();
      json.put("coveredDaysRange", coveredDaysRange);
    }
    json.put("fullDay", getModel().isFullDay());
    json.put("draggable", getModel().getProvider().isMoveItemEnabled());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref", null);
    getModel().getCalendar().getUIFacade().fireAppLinkActionFromUI(ref);
  }
}
