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
