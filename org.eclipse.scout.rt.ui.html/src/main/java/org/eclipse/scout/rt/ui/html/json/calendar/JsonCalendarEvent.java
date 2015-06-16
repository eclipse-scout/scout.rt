package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonCalendarEvent implements IJsonObject {

  private final JsonCalendar m_jsonCalendar;
  private final CalendarEvent m_event;

  public JsonCalendarEvent(JsonCalendar<?> jsonCalendar, CalendarEvent event) {
    m_jsonCalendar = jsonCalendar;
    m_event = event;
  }

  public final JsonCalendar getJsonCalendar() {
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
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    json.put("type", m_event.getType());
    json.put("component", m_jsonCalendar.getAdapter(m_event.getComponent()).getId());
    json.put("popupMenus", m_event.getPopupMenus()); // TODO BSH Calendar | Convert JSON menus
    return json;
  }
}
