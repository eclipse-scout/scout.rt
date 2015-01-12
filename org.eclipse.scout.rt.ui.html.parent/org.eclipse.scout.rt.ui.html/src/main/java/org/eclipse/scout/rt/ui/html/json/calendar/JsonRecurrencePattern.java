package org.eclipse.scout.rt.ui.html.json.calendar;

import org.eclipse.scout.rt.shared.services.common.calendar.RecurrencePattern;
import org.eclipse.scout.rt.ui.html.json.IJsonMapper;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonRecurrencePattern implements IJsonMapper {

  private final RecurrencePattern m_recurrencePattern;

  public JsonRecurrencePattern(RecurrencePattern recurrencePattern) {
    m_recurrencePattern = recurrencePattern;
  }

  public final RecurrencePattern getRecurrencePattern() {
    return m_recurrencePattern;
  }

  @Override
  public JSONObject toJson() {
    if (m_recurrencePattern == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "lastModified", m_recurrencePattern.getLastModified());
    JsonObjectUtility.putProperty(json, "regenerate", m_recurrencePattern.isRegenerate());
    JsonObjectUtility.putProperty(json, "startTimeMinutes", m_recurrencePattern.getStartTimeMinutes());
    JsonObjectUtility.putProperty(json, "endTimeMinutes", m_recurrencePattern.getEndTimeMinutes());
    JsonObjectUtility.putProperty(json, "durationMinutes", m_recurrencePattern.getDurationMinutes());
    JsonObjectUtility.putProperty(json, "firstDate", new JsonDate(m_recurrencePattern.getFirstDate()).asJsonString());
    JsonObjectUtility.putProperty(json, "lastDate", new JsonDate(m_recurrencePattern.getLastDate()).asJsonString());
    JsonObjectUtility.putProperty(json, "occurrences", m_recurrencePattern.getOccurrences());
    JsonObjectUtility.putProperty(json, "noEndDate", m_recurrencePattern.getNoEndDate());
    JsonObjectUtility.putProperty(json, "type", m_recurrencePattern.getType());
    JsonObjectUtility.putProperty(json, "interval", m_recurrencePattern.getInterval());
    JsonObjectUtility.putProperty(json, "instance", m_recurrencePattern.getInstance());
    JsonObjectUtility.putProperty(json, "dayOfWeekBits", m_recurrencePattern.getDayOfWeek());
    JsonObjectUtility.putProperty(json, "dayOfMonth", m_recurrencePattern.getDayOfMonth());
    JsonObjectUtility.putProperty(json, "monthOfYear", m_recurrencePattern.getMonthOfYear());
    // TODO BSH Add RecurrenceException?
    return json;
  }
}
