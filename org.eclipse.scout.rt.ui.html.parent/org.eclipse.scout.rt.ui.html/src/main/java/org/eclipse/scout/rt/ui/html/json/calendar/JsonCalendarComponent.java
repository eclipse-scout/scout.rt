package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.html.json.IJsonMapper;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.json.JSONObject;

public class JsonCalendarComponent implements IJsonMapper {
  private final IJsonSession m_session;
  private final CalendarComponent m_component;

  public JsonCalendarComponent(IJsonSession session, CalendarComponent component) {
    m_session = session;
    m_component = component;
  }

  public IJsonSession getJsonSession() {
    return m_session;
  }

  public final CalendarComponent getComponent() {
    return m_component;
  }

  @Override
  public JSONObject toJson() {
    if (m_component == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "item", new JsonCalendarItem(m_component.getItem()).toJson());
    JsonObjectUtility.putProperty(json, "cell", new JsonCell(getJsonSession(), m_component.getCell()).toJson());
    JsonObjectUtility.putProperty(json, "fromDate", new JsonDate(m_component.getFromDate()).asJsonString());
    JsonObjectUtility.putProperty(json, "toDate", new JsonDate(m_component.getToDate()).asJsonString());
    JsonObjectUtility.putProperty(json, "coveredDays", JsonObjectUtility.newJSONArray(m_component.getCoveredDays()));
    JsonObjectUtility.putProperty(json, "fullDay", m_component.isFullDay());
    JsonObjectUtility.putProperty(json, "draggable", m_component.getProvider().isMoveItemEnabled());
    return json;
  }
}
