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
package org.eclipse.scout.rt.ui.html.json.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.json.JSONObject;

public class JsonTableField extends JsonFormField<ITableField<? extends ITable>> {
  private static final String PROP_TABLE_ID = "tableId";

  public JsonTableField(ITableField<? extends ITable> model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "TableField";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
  }

  @Override
  public JSONObject toJson() {
    //FIXME when to dispose table? maybe introduce dispose event for table fields -> dont dispose for outlineTables, dispose otherwise
    return putProperty(super.toJson(), ITableField.PROP_TABLE, modelObjectToJson(getModelObject().getTable()));
  }

  protected void handleModelTableChanged(ITable table) {
    JsonTable jsonTable = (JsonTable) getJsonSession().getJsonAdapter(table);
    if (jsonTable == null) {
      jsonTable = (JsonTable) getJsonSession().createJsonAdapter(table);
      getJsonSession().currentJsonResponse().addCreateEvent(getId(), jsonTable.toJson());
    }
    else {
      JSONObject jsonEvent = new JSONObject();
      putProperty(jsonEvent, PROP_TABLE_ID, jsonTable.getId());
      getJsonSession().currentJsonResponse().addActionEvent("tableChanged", getId(), jsonEvent);
    }
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    super.handleModelPropertyChange(propertyName, newValue);
    if (ITableField.PROP_TABLE.equals(propertyName)) {
      handleModelTableChanged((ITable) newValue);
    }
  }
}
