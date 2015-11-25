/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.calendar;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
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
    if (getModel().getCoveredDays() != null) {
      for (Date coveredDay : getModel().getCoveredDays()) {
        json.append("coveredDays", new JsonDate(coveredDay).asJsonString());
      }
    }
    json.put("fullDay", getModel().isFullDay());
    json.put("draggable", getModel().getProvider().isMoveItemEnabled());
    return json;
  }

}
