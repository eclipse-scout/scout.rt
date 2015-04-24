package org.eclipse.scout.rt.ui.html.json.basic.planner;

import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONObject;

public class JsonActivityCell implements IJsonObject {

  private final IIdProvider<Activity<?, ?>> m_idProvider;
  private final Activity<?, ?> m_activityCell;

  public JsonActivityCell(Activity<?, ?> cell, IIdProvider<Activity<?, ?>> idProvider) {
    m_activityCell = cell;
    m_idProvider = idProvider;
  }

  public Activity<?, ?> getActivityCell() {
    return m_activityCell;
  }

  @Override
  public JSONObject toJson() {
    if (m_activityCell == null) {
      return null;
    }
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    JsonObjectUtility.putProperty(json, "id", m_idProvider.getId(m_activityCell));
    JsonObjectUtility.putProperty(json, "beginTime", new JsonDate(m_activityCell.getBeginTime()).asJsonString());
    JsonObjectUtility.putProperty(json, "endTime", new JsonDate(m_activityCell.getEndTime()).asJsonString());
    JsonObjectUtility.putProperty(json, "text", m_activityCell.getText());
    JsonObjectUtility.putProperty(json, "backgroundColor", m_activityCell.getBackgroundColor());
    JsonObjectUtility.putProperty(json, "foregroundColor", m_activityCell.getForegroundColor());
    JsonObjectUtility.putProperty(json, "level", m_activityCell.getLevel());
    JsonObjectUtility.putProperty(json, "levelColor", m_activityCell.getLevelColor());
    JsonObjectUtility.putProperty(json, "durationMinutes", m_activityCell.getDurationMinutes());
    JsonObjectUtility.putProperty(json, "tooltipText", m_activityCell.getTooltipText());
    JsonObjectUtility.putProperty(json, "iconId", BinaryResourceUrlUtility.createIconUrl(m_activityCell.getIconId()));
    JsonObjectUtility.filterDefaultValues(json, "Activity");
    return json;
  }
}
