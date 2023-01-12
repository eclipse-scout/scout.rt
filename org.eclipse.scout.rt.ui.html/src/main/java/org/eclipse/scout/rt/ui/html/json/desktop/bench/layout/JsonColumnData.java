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
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.FlexboxLayoutData;
import org.json.JSONObject;

/**
 * @author Andreas Hoegger
 */
public class JsonColumnData extends JsonLayoutData {

  public JsonColumnData(BenchColumnData layoutData) {
    super(layoutData);
  }

  @Override
  public BenchColumnData getLayoutData() {
    return (BenchColumnData) super.getLayoutData();
  }

  @Override
  public JSONObject toJson() {
    if (getLayoutData() == null) {
      return null;
    }
    JSONObject json = super.toJson();
    for (FlexboxLayoutData rowData : getLayoutData().getRows()) {
      json.append("rows", JsonLayoutData.toJson(rowData));
    }
    return json;
  }

  public static JSONObject toJson(BenchColumnData layoutData) {
    if (layoutData == null) {
      return null;
    }
    return new JsonColumnData(layoutData).toJson();
  }
}
