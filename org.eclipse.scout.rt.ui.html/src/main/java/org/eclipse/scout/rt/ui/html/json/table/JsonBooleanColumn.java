/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.json.JSONObject;

public class JsonBooleanColumn<T extends IBooleanColumn> extends JsonColumn<T> {

  public JsonBooleanColumn(T model) {
    super(model);
  }

  @Override
  public String getObjectType() {
    return "BooleanColumn";
  }

  @Override
  public boolean isValueRequired() {
    return true;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(IBooleanColumn.PROP_TRI_STATE_ENABLED, getColumn().isTriStateEnabled());
    return json;
  }
}
