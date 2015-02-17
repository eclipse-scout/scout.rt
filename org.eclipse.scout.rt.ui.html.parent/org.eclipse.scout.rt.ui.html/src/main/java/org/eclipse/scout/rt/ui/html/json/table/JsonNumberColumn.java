/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonNumberColumn<T extends INumberColumn<?>> extends JsonColumn<T> {

  public JsonNumberColumn(T model, IJsonSession jsonSession) {
    super(model, jsonSession);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonObjectUtility.putProperty(json, "format", ((INumberColumn) getColumn()).getFormat().toPattern());
    return json;
  }

  @Override
  public Object cellValueToJson(Object value) {
    return value;
  }
}
