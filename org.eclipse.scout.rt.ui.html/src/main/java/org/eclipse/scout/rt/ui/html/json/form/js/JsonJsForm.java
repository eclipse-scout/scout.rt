/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.js;

import org.eclipse.scout.rt.client.ui.form.js.IJsForm;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.json.JSONObject;

public class JsonJsForm<IN extends IDataObject, OUT extends IDataObject, T extends IJsForm<IN, OUT>> extends JsonForm<T> {

  private static final String EVENT_SAVE = "save";

  private final JsonDataObjectHelper m_jsonDoHelper = BEANS.get(JsonDataObjectHelper.class); // cached instance

  public JsonJsForm(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "JsForm";
  }

  @Override
  protected void initJsonProperties(T model) {
    putJsonProperty(new JsonProperty<IJsForm<IN, OUT>>(IJsForm.PROP_INPUT_DATA, model) {
      @Override
      protected IN modelValue() {
        return getModel().getInputData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return jsonDoHelper().dataObjectToJson((IDoEntity) value);
      }
    });
    putJsonProperty(new JsonProperty<IJsForm<IN, OUT>>(IJsForm.PROP_JS_FORM_OBJECT_TYPE, model) {
      @Override
      protected String modelValue() {
        return getModel().getJsFormObjectType();
      }
    });
    putJsonProperty(new JsonProperty<IJsForm<IN, OUT>>(IJsForm.PROP_JS_FORM_MODEL, model) {
      @Override
      protected IDoEntity modelValue() {
        return getModel().getJsFormModel();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return jsonDoHelper().dataObjectToJson((IDoEntity) value);
      }
    });
  }

  protected JsonDataObjectHelper jsonDoHelper() {
    return m_jsonDoHelper;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SAVE.equals(event.getType())) {
      handleUiSave(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSave(JsonEvent event) {
    JSONObject outputDataJson = event.getData().optJSONObject("outputData");
    OUT outputData = jsonDoHelper().jsonToDataObject(outputDataJson, getModel().getOutputDataType());
    getModel().getUIFacade().fireSaveFromUI(outputData);
  }
}
