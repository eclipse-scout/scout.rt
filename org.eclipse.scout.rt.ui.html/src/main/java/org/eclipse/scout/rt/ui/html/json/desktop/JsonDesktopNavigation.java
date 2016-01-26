package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONObject;

public class JsonDesktopNavigation implements IJsonObject {

  private IDesktopNotification m_notification;

  public JsonDesktopNavigation(IDesktopNotification notification) {
    m_notification = notification;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("status", JsonStatus.toJson(m_notification.getStatus()));
    json.put("duration", m_notification.getDuration());
    json.put("closeable", m_notification.isCloseable());
    return json;
  }

  public static JSONObject toJson(IDesktopNotification notification) {
    if (notification == null) {
      return null;
    }
    return new JsonDesktopNavigation(notification).toJson();
  }

}
