package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONObject;

public class JsonDesktopNotification implements IJsonObject {

  private IDesktopNotification m_notification;

  public JsonDesktopNotification(IDesktopNotification notification) {
    m_notification = notification;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("status", JsonStatus.toJson(m_notification.getStatus()));
    json.put("duration", m_notification.getDuration());
    json.put("closeable", m_notification.isCloseable());
    json.put("id", getNotificationId(m_notification));
    return json;
  }

  /**
   * Returns a unique ID for the desktop notification. For convenience we use the identityHashCode() method here.
   * Because event when two notifications instances are identical, we want to send them to the UI and must be able to
   * address them individually. Also we don't want to rely on a the hashCode() method of the notification object itself,
   * because it would typically return the same hash-code when two instances are identical.
   * <p>
   * Additionally: we don't want to create a json-adapter for the notification model object. Since we don't want to keep
   * the state of the notifications in the Java model. Because we had to work with timers/jobs if we had to deal with
   * the duration property.
   */
  public static JSONObject toNotificationIdJson(IDesktopNotification notification) {
    if (notification == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("id", getNotificationId(notification));
    return json;
  }

  protected static String getNotificationId(IDesktopNotification notification) {
    return String.valueOf(System.identityHashCode(notification));
  }

  public static JSONObject toJson(IDesktopNotification notification) {
    if (notification == null) {
      return null;
    }
    return new JsonDesktopNotification(notification).toJson();
  }

}
