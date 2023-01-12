/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.tile.TileGridLayoutConfig;
import org.eclipse.scout.rt.ui.html.json.JsonLogicalGridLayoutConfig;
import org.json.JSONObject;

public class JsonTileGridLayoutConfig extends JsonLogicalGridLayoutConfig {

  public JsonTileGridLayoutConfig(LogicalGridLayoutConfig layoutConfig) {
    super(layoutConfig);
  }

  @Override
  public TileGridLayoutConfig getLayoutConfig() {
    return (TileGridLayoutConfig) super.getLayoutConfig();
  }

  @Override
  public JSONObject toJson() {
    if (getLayoutConfig() == null) {
      return null;
    }
    JSONObject json = super.toJson();
    json.put("maxWidth", getLayoutConfig().getMaxWidth());
    return json;
  }
}
