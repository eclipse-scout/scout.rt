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

import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.json.JSONObject;

@SuppressWarnings("squid:S00118")
public abstract class JsonTableUserFilter<T extends IUserFilterState> implements IJsonObject {
  private final T m_filter;
  private JsonTable<?> m_jsonTable;

  public JsonTableUserFilter(T filter) {
    m_filter = filter;
  }

  public void setJsonTable(JsonTable<?> jsonTable) {
    m_jsonTable = jsonTable;
  }

  public JsonTable<?> getJsonTable() {
    return m_jsonTable;
  }

  public T getFilterState() {
    return m_filter;
  }

  public boolean isValid() {
    return true;
  }

  public abstract String getObjectType();

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("objectType", getObjectType());
    json.put("filterType", getFilterState().getType());
    return json;
  }
}
