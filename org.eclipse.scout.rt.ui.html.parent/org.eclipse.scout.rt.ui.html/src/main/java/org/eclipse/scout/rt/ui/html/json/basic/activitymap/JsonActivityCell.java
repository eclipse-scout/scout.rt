package org.eclipse.scout.rt.ui.html.json.basic.activitymap;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.ui.html.json.IJsonMapper;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonActivityCell<RI, AI> implements IJsonMapper {

  private final ActivityCell<RI, AI> m_activityCell;

  public JsonActivityCell(ActivityCell<RI, AI> activityCell) {
    m_activityCell = activityCell;
  }

  public ActivityCell<RI, AI> getActivityCell() {
    return m_activityCell;
  }

  @Override
  public JSONObject toJson() {
    if (m_activityCell == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    JsonObjectUtility.putProperty(json, "resourceId", m_activityCell.getResourceId()); // TODO BSH How to convert to JSON?
    JsonObjectUtility.putProperty(json, "activityId", m_activityCell.getActivityId()); // TODO BSH How to convert to JSON?
    JsonObjectUtility.putProperty(json, "beginTime", new JsonDate(m_activityCell.getBeginTime()).asJsonString());
    JsonObjectUtility.putProperty(json, "endTime", new JsonDate(m_activityCell.getEndTime()).asJsonString());
    JsonObjectUtility.putProperty(json, "text", m_activityCell.getText());
    JsonObjectUtility.putProperty(json, "backgroundColor", m_activityCell.getBackgroundColor());
    JsonObjectUtility.putProperty(json, "foregroundColor", m_activityCell.getForegroundColor());
    JsonObjectUtility.putProperty(json, "majorValue", m_activityCell.getMajorValue());
    JsonObjectUtility.putProperty(json, "majorColor", m_activityCell.getMajorColor());
    JsonObjectUtility.putProperty(json, "minorValue", m_activityCell.getMinorValue());
    JsonObjectUtility.putProperty(json, "minorColor", m_activityCell.getMinorColor());
    JsonObjectUtility.putProperty(json, "durationMinutes", m_activityCell.getDurationMinutes());
    JsonObjectUtility.putProperty(json, "tooltipText", m_activityCell.getTooltipText());
    JsonObjectUtility.putProperty(json, "iconId", m_activityCell.getIconId());
    JsonObjectUtility.putProperty(json, "customData", m_activityCell.getCustomData());
    return json;
  }
}
