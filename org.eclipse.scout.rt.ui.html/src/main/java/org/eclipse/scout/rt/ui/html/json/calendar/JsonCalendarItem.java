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
    json.put("exists", m_item.exists());
    json.put("lastModified", m_item.getLastModified());
    json.put("itemId", m_item.getItemId());
    json.put("owner", m_item.getOwner());
    json.put("subject", m_item.getSubject());
    json.put("body", m_item.getBody());
    json.put("cssClass", m_item.getCssClass());
    json.put("recurrencePattern", new JsonRecurrencePattern(m_item.getRecurrencePattern()).toJson());
    return json;
  }
}
