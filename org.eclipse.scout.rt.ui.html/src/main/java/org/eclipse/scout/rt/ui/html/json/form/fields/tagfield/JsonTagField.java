/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.tagfield;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField;
import org.eclipse.scout.rt.client.ui.form.fields.tagfield.TagFieldValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonTagField extends JsonValueField<ITagField> {

  public JsonTagField(ITagField model, IUiSession uiSession, String id, IJsonAdapter parent) {
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
      protected TagFieldValue modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return new JSONArray();
        }
        Set<String> tags = ((TagFieldValue) value).getTags();
        return new JSONArray(tags);
      }
    });
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    JSONObject data = event.getData();
    TagFieldValue valueFromUi = jsonToValue(data.opt(IValueField.PROP_VALUE));
    handleUiValueChange(data);

    // In case the model changes its value to something other than what the UI
    // sends, we cannot set display text and error status. This can happen if
    // execValidateValue is overridden.
    TagFieldValue valueFromModel = getModel().getValue();
    if (!ObjectUtility.equals(valueFromUi, valueFromModel)) {
      return;
    }

    handleUiDisplayTextChange(data);
  }

  @Override
  protected TagFieldValue jsonToValue(Object jsonValue0) {
    JSONArray jsonValue = (JSONArray) jsonValue0;
    int numTags = jsonValue.length();
    Set<String> tags = new HashSet<>(numTags);
    for (int i = 0; i < numTags; i++) {
      tags.add(jsonValue.getString(i));
    }
    return new TagFieldValue(tags);
  }

  @Override
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueFromUI((TagFieldValue) value);
  }

}
