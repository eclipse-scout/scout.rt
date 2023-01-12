/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.json.JSONObject;

public class JsonLogicalGridLayoutConfig implements IJsonObject {

  private final LogicalGridLayoutConfig m_layoutConfig;

  public JsonLogicalGridLayoutConfig(LogicalGridLayoutConfig layoutConfig) {
    m_layoutConfig = layoutConfig;
  }

  public LogicalGridLayoutConfig getLayoutConfig() {
    return m_layoutConfig;
  }

  @Override
  public JSONObject toJson() {
    if (m_layoutConfig == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("hgap", m_layoutConfig.getHGap());
    json.put("vgap", m_layoutConfig.getVGap());
    json.put("rowHeight", m_layoutConfig.getRowHeight());
    json.put("columnWidth", m_layoutConfig.getColumnWidth());
    json.put("minWidth", m_layoutConfig.getMinWidth());
    return json;
  }
}
