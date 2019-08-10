/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
