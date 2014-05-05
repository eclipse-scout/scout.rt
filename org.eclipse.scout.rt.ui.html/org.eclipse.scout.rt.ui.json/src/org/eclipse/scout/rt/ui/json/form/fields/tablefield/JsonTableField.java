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
package org.eclipse.scout.rt.ui.json.form.fields.tablefield;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.json.table.JsonTable;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTableField extends JsonFormField<ITableField<? extends ITable>> {
  private static final String PROP_TABLE_ID = "tableId";

  private Map<ITable, JsonTable> m_jsonTables;

  public JsonTableField(ITableField<? extends ITable> model, IJsonSession session) {
    super(model, session);
    m_jsonTables = new HashMap<>();
  }

  @Override
  protected void attachModel() throws JsonException {
    super.attachModel();

    //FIXME Hold JsonTable globally? and share with other elements like desktop? generally hold every model object globally? when to dispose? only on model dispose?
    //-> incremental creation of the model -> improves offline, maybe introduce dispose flag for table fields -> dont dispose for outlineTables, dispose otherwise
    createAndRegisterJsonTable(getModelObject().getTable());
  }

  @Override
  public JSONObject toJson() throws JsonException {
    JSONObject json = super.toJson();

    try {
      json.put(ITableField.PROP_TABLE, m_jsonTables.get(getModelObject().getTable()).toJson());
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
  }

  protected JsonTable createAndRegisterJsonTable(ITable table) {
    JsonTable jsonTable = JsonRendererFactory.get().createJsonTable(table, getJsonSession());
    m_jsonTables.put(table, jsonTable);

    return jsonTable;
  }

  protected String disposeAndUnregisterJsonTable(ITable table) {
    JsonTable jsonTable = m_jsonTables.remove(table);
    jsonTable.dispose();

    return jsonTable.getId();
  }

  protected void handleModelTableChanged(ITable table) {
    try {
      JsonTable jsonTable = m_jsonTables.get(table);
      if (jsonTable == null) {
        jsonTable = createAndRegisterJsonTable(table);

        getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonTable.toJson());
      }
      else {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(PROP_TABLE_ID, jsonTable.getId());
        getJsonSession().currentJsonResponse().addActionEvent("tableChanged", getId(), jsonEvent);
      }
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  @Override
  protected void handleModelPropertyChange(String name, Object newValue) {
    super.handleModelPropertyChange(name, newValue);

    if (name.equals(ITableField.PROP_TABLE)) {
      handleModelTableChanged((ITable) newValue);
    }
  }
}
