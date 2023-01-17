/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

/**
 * This class creates JSON output for an IBooleanField used as a check-box.
 */
public class JsonCheckBoxField<CHECK_BOX_FIELD extends IBooleanField> extends JsonValueField<CHECK_BOX_FIELD> {

  public JsonCheckBoxField(CHECK_BOX_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CheckBoxField";
  }

  @Override
  protected void initJsonProperties(CHECK_BOX_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBooleanField>(IBooleanField.PROP_VALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getValue();
      }
    });
    putJsonProperty(new JsonProperty<IBooleanField>(IBooleanField.PROP_TRI_STATE_ENABLED, model) {
      @Override
      protected Object modelValue() {
        return getModel().isTriStateEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IBooleanField>(IBooleanField.PROP_KEY_STROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getKeyStroke();
      }
    });
    putJsonProperty(new JsonProperty<IBooleanField>(IBooleanField.PROP_WRAP_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWrapText();
      }
    });

    // No need to send display text for check box
    removeJsonProperty(IBooleanField.PROP_DISPLAY_TEXT);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IBooleanField.PROP_VALUE.equals(propertyName)) {
      Object o = data.opt(propertyName);
      Boolean uiValue = null;
      if (ObjectUtility.isOneOf(o, Boolean.TRUE, Boolean.FALSE)) {
        uiValue = (Boolean) o;
      }
      addPropertyEventFilterCondition(propertyName, uiValue);
      getModel().getUIFacade().setValueFromUI(uiValue);

      // In some cases the widget in the UI is clicked, which causes the check-box to be de-/selected, but the model rejects the value-change.
      // in that case we must "revert" the click in the UI, so that UI and model are in-sync again. This may happen, when the model-field throws
      // a VetoExeception in its execValidateValue() method.
      Boolean modelValue = getModel().getValue();
      if (ObjectUtility.notEquals(uiValue, modelValue)) {
        addPropertyChangeEvent(propertyName, modelValue);
      }
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
