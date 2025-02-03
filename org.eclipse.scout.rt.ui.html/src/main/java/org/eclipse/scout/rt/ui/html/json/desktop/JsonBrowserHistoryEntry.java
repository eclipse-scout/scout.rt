/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonBrowserHistoryEntry implements IJsonObject {

  private final BrowserHistoryEntry m_browserHistoryEntry;

  public JsonBrowserHistoryEntry(BrowserHistoryEntry browserHistoryEntry) {
    m_browserHistoryEntry = browserHistoryEntry;
  }

  @Override
  public Object toJson() {
    JSONObject json = new JSONObject();
    json.put("path", m_browserHistoryEntry.getPath());
    json.put("title", m_browserHistoryEntry.getTitle());
    json.put("deepLinkPath", m_browserHistoryEntry.getDeepLinkPath());
    json.put("pathVisible", m_browserHistoryEntry.isPathVisible());
    return json;
  }

  public static Object toJson(BrowserHistoryEntry browserHistoryEntry) {
    if (browserHistoryEntry == null) {
      return null;
    }
    return new JsonBrowserHistoryEntry(browserHistoryEntry).toJson();
  }
}
