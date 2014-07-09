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
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonTableField extends JsonFormField<ITableField<? extends ITable>> {

  public JsonTableField(ITableField<? extends ITable> model, IJsonSession session, String id) {
    super(model, session, id);

    putJsonProperty(new JsonAdapterProperty<ITableField<? extends ITable>>(ITableField.PROP_TABLE, model, session) {
      @Override
      protected ITable modelValue() {
        return getModel().getTable();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    return super.toJson();
  }

  @Override
  public String getObjectType() {
    return "TableField";
  }

  @Override
  public void dispose() {
    disposeJsonAdapter(getModel().getTable());
    super.dispose();
  }
}
