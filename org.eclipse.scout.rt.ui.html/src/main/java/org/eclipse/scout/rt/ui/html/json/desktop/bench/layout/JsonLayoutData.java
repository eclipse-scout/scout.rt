/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.bench.layout;

import org.eclipse.scout.rt.client.ui.desktop.bench.layout.FlexboxLayoutData;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

/**
 * @author Andreas Hoegger
 */
public class JsonLayoutData implements IJsonObject {

  private final FlexboxLayoutData m_layoutData;

  public JsonLayoutData(FlexboxLayoutData layoutData) {
    m_layoutData = layoutData;
  }

  public FlexboxLayoutData getLayoutData() {
    return m_layoutData;
  }

  @Override
  public JSONObject toJson() {
    if (m_layoutData == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("initial", m_layoutData.getInitial());
    json.put("grow", m_layoutData.getGrow());
    json.put("shrink", m_layoutData.getShrink());
    json.put("relative", m_layoutData.isRelative());
    return json;
  }

  public static JSONObject toJson(FlexboxLayoutData layoutData) {
    if (layoutData == null) {
      return null;
    }
    return new JsonLayoutData(layoutData).toJson();
  }
}
