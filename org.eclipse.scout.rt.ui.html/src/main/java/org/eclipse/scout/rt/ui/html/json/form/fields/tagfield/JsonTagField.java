/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tagfield;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.json.lookup.JsonLookupCallResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTagField extends JsonValueField<ITagField> {

  public JsonTagField(ITagField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TagField";
  }

  @Override
  protected void initJsonProperties(ITagField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ITagField>(IValueField.PROP_VALUE, model) {
      @Override
      protected Set<String> modelValue() {
        return getModel().getValue();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return new JSONArray();
        }
        return new JSONArray((Set<String>) value);
      }
    });
    putJsonProperty(new JsonProperty<ITagField>(ITagField.PROP_RESULT, model) {
      @Override
      protected ILookupCallResult<String> modelValue() {
        return getModel().getResult();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return JsonLookupCallResult.toJson((ILookupCallResult<String>) value);
      }
    });
    putJsonProperty(new JsonProperty<ITagField>(ITagField.PROP_MAX_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxLength();
      }
    });
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    JSONObject data = event.getData();
    Set<String> valueFromUi = jsonToValue(data.opt(IValueField.PROP_VALUE));
    handleUiValueChange(data);

    // In case the model changes its value to something other than what the UI
    // sends, we cannot set display text and error status. This can happen if
    // execValidateValue is overridden.
    Set<String> valueFromModel = getModel().getValue();
    if (!ObjectUtility.equals(valueFromUi, valueFromModel)) {
      return;
    }

    handleUiDisplayTextChange(data);
  }

  protected void handleUiLookupByText(JsonEvent event) {
    String text = event.getData().optString("text");
    getModel().getUIFacade().lookupByTextFromUI(text);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("lookupByText".equals(event.getType())) {
      handleUiLookupByText(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected Set<String> jsonToValue(Object jsonValue0) {
    JSONArray jsonValue = (JSONArray) jsonValue0;
    int numTags = jsonValue.length();
    Set<String> tags = new HashSet<>(numTags);
    for (int i = 0; i < numTags; i++) {
      tags.add(jsonValue.getString(i));
    }
    return tags;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueFromUI((Set<String>) value);
  }
}
