/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop.bench.layout;

import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchColumnData;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchLayoutData;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.json.JSONObject;

public class JsonBenchLayoutData implements IJsonObject {

  private BenchLayoutData m_layoutData;

  public JsonBenchLayoutData(BenchLayoutData layoutdata) {
    m_layoutData = layoutdata;
  }

  @Override
  public JSONObject toJson() {
    if (m_layoutData == null) {
      return null;
    }
    JSONObject json = new JSONObject();
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
