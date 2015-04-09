package org.eclipse.scout.rt.ui.html.json.basic.activitymap;

import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonTimeScale implements IJsonObject {

  private final TimeScale m_timeScale;

  public JsonTimeScale(TimeScale timeScale) {
    m_timeScale = timeScale;
  }

  public TimeScale getTimeScale() {
    return m_timeScale;
  }

  @Override
  public JSONObject toJson() {
    if (m_timeScale == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    // TODO BSH Fill object
    return json;
  }
}
