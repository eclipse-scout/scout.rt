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
