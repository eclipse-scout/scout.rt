package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.BrowserHistory;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonBrowserHistory implements IJsonObject {

  private BrowserHistory m_browserHistory;

  public JsonBrowserHistory(BrowserHistory browserHistory) {
    m_browserHistory = browserHistory;
  }

  @Override
  public Object toJson() {
    JSONObject json = new JSONObject();
    json.put("path", m_browserHistory.getPath());
    json.put("title", m_browserHistory.getTitle());
    return json;
  }

  public static Object toJson(BrowserHistory browserHistory) {
    if (browserHistory == null) {
      return null;
    }
    return new JsonBrowserHistory(browserHistory).toJson();
  }

}
