/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.menu.form.field;

import org.eclipse.scout.rt.client.ui.action.menu.form.fields.IFormFieldMenu;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.json.JSONObject;

public class JsonFormFieldMenu<MENU extends IFormFieldMenu> extends JsonMenu<MENU> {
  private static final String PROP_FIELD = "field";

  public JsonFormFieldMenu(MENU model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getField());
  }

  @Override
  public String getObjectType() {
    return "FormFieldMenu";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, PROP_FIELD, getModel().getField());
    return json;
  }
}
