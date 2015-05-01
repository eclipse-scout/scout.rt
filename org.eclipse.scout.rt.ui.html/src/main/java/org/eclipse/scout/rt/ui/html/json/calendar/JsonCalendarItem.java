package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonCalendarItem implements IJsonObject {

  private final ICalendarItem m_item;

  public JsonCalendarItem(ICalendarItem item) {
    m_item = item;
  }

  public final ICalendarItem getItem() {
    return m_item;
  }

  @Override
  public JSONObject toJson() {
    if (m_item == null) {
      return null;
    }
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    JsonObjectUtility.putProperty(json, "exists", m_item.exists());
    JsonObjectUtility.putProperty(json, "lastModified", m_item.getLastModified());
    JsonObjectUtility.putProperty(json, "itemId", m_item.getItemId());
    JsonObjectUtility.putProperty(json, "owner", m_item.getOwner());
    JsonObjectUtility.putProperty(json, "subject", m_item.getSubject());
    JsonObjectUtility.putProperty(json, "body", m_item.getBody());
    JsonObjectUtility.putProperty(json, "cssClass", m_item.getCssClass());
    JsonObjectUtility.putProperty(json, "recurrencePattern", new JsonRecurrencePattern(m_item.getRecurrencePattern()).toJson());
    return json;
  }
}
