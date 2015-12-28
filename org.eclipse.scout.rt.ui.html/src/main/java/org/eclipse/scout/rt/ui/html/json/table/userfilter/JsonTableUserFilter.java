/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.json.JSONObject;

public abstract class JsonTableUserFilter<T extends IUserFilterState> implements IJsonObject {
  private T m_filter;
  private JsonTable m_jsonTable;

  public JsonTableUserFilter(T filter) {
    m_filter = filter;
  }

  public void setJsonTable(JsonTable jsonTable) {
    m_jsonTable = jsonTable;
  }

  public JsonTable getJsonTable() {
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
