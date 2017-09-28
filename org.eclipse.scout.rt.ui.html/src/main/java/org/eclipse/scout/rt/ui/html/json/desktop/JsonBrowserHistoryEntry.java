/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    return json;
  }

  public static Object toJson(BrowserHistoryEntry browserHistoryEntry) {
    if (browserHistoryEntry == null) {
      return null;
    }
    return new JsonBrowserHistoryEntry(browserHistoryEntry).toJson();
  }

}
