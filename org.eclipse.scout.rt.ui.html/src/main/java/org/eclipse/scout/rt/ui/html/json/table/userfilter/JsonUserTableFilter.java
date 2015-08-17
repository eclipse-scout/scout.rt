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
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.IUserTableFilter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.json.JSONObject;

public class JsonUserTableFilter<T extends IUserTableFilter> implements IJsonObject {
  private T m_filter;
  private JsonTable m_jsonTable;

  public JsonUserTableFilter(T filter) {
    m_filter = filter;
  }

  public void setJsonTable(JsonTable jsonTable) {
    m_jsonTable = jsonTable;
  }

  public JsonTable getJsonTable() {
    return m_jsonTable;
  }

  public T getFilter() {
    return m_filter;
  }

  public String getObjectType() {
    return "UserTableFilter";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = JsonObjectUtility.newOrderedJSONObject();
    json.put("objectType", getObjectType());
    json.put("filterType", getFilter().getType());
    return json;
  }
}
