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
