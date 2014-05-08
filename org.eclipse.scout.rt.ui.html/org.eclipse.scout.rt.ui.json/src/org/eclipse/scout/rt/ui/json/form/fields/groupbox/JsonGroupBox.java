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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonRendererFactory;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.eclipse.scout.rt.ui.json.form.fields.IJsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGroupBox extends JsonFormField<IGroupBox> {
  private List<IJsonFormField> m_jsonFormFields;

  public JsonGroupBox(IGroupBox model, IJsonSession session) {
    super(model, session);
    m_jsonFormFields = new LinkedList<>();
  }

  @Override
  public String getObjectType() {
    return "GroupBox";
  }

  @Override
  protected void attachModel() {
    super.attachModel();

    for (IFormField field : getModelObject().getControlFields()) {
      IJsonFormField jsonFormField = JsonRendererFactory.get().createJsonFormField(field, getJsonSession());
      m_jsonFormFields.add(jsonFormField);
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    try {
      json.put("borderDecoration", getModelObject().getBorderDecoration());
      json.put("borderVisible", getModelObject().isBorderVisible());
      JSONArray formFields = new JSONArray();
      for (IJsonFormField jsonFormField : m_jsonFormFields) {
        formFields.put(jsonFormField.toJson());
      }
      json.put("formFields", formFields);

      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

}
