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

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonCalendarEvent implements IJsonObject {

  private final JsonCalendar<?> m_jsonCalendar;
  private final CalendarEvent m_event;

  public JsonCalendarEvent(JsonCalendar<?> jsonCalendar, CalendarEvent event) {
    m_jsonCalendar = jsonCalendar;
    m_event = event;
  }

  public final JsonCalendar<?> getJsonCalendar() {
    return m_jsonCalendar;
  }

  public final CalendarEvent getEvent() {
    return m_event;
  }

  @Override
  public JSONObject toJson() {
    if (m_event == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("type", m_event.getType());
    json.put("component", m_jsonCalendar.getAdapter(m_event.getComponent()).getId());
    json.put("popupMenus", m_event.getPopupMenus()); // TODO [7.0] bsh: Calendar | Convert JSON menus
    return json;
  }
}
