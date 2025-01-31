/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.json.JSONObject;

public class JsonTableTextUserFilter<T extends TableTextUserFilterState> extends JsonTableUserFilter<T> {

  public JsonTableTextUserFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "TableTextUserFilter";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("text", getFilterState().getText());
    return json;
  }
}
