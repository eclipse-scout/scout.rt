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
    json.put("id", m_idProvider.getId(m_activityCell));
    json.put("beginTime", new JsonDate(m_activityCell.getBeginTime()).asJsonString());
    json.put("endTime", new JsonDate(m_activityCell.getEndTime()).asJsonString());
    json.put("text", m_activityCell.getText());
    json.put("backgroundColor", m_activityCell.getBackgroundColor());
    json.put("foregroundColor", m_activityCell.getForegroundColor());
    json.put("level", m_activityCell.getLevel());
    json.put("levelColor", m_activityCell.getLevelColor());
    json.put("durationMinutes", m_activityCell.getDurationMinutes());
    json.put("tooltipText", m_activityCell.getTooltipText());
    json.put("iconId", BinaryResourceUrlUtility.createIconUrl(m_activityCell.getIconId()));
    JsonObjectUtility.filterDefaultValues(json, "Activity");
    return json;
  }
}
