/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonTableField<TABLE_FIELD extends ITableField<? extends ITable>> extends JsonFormField<TABLE_FIELD> {

  public JsonTableField(TABLE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TableField";
  }

  @Override
  protected void initJsonProperties(TABLE_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<TABLE_FIELD>(ITableField.PROP_TABLE, model, getUiSession()) {
      @Override
      protected ITable modelValue() {
        return getModel().getTable();
      }
    });
  }
}
