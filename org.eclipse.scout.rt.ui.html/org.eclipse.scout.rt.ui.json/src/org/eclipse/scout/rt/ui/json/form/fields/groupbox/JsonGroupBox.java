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
package org.eclipse.scout.rt.ui.json.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonGroupBox extends JsonFormField<IGroupBox> {

  public JsonGroupBox(IGroupBox model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "GroupBox";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    IGroupBox model = getModelObject();
    putProperty(json, "borderDecoration", model.getBorderDecoration());
    putProperty(json, "borderVisible", model.isBorderVisible());
    putProperty(json, "formFields", modelObjectsToJson(model.getControlFields()));
    return json;
  }

}
