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

import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchColumnData;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchLayoutData;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonBenchLayoutData implements IJsonObject {

  private final BenchLayoutData m_layoutData;

  public JsonBenchLayoutData(BenchLayoutData layoutdata) {
    m_layoutData = layoutdata;
  }

  @Override
  public JSONObject toJson() {
    if (m_layoutData == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("cacheKey", m_layoutData.getCacheKey());
    for (BenchColumnData col : m_layoutData.getColumns()) {
      json.append("columns", JsonColumnData.toJson(col));
    }
    return json;
  }

  public static JSONObject toJson(BenchLayoutData layoutData) {
    if (layoutData == null) {
      return null;
    }
    return new JsonBenchLayoutData(layoutData).toJson();
  }
}
