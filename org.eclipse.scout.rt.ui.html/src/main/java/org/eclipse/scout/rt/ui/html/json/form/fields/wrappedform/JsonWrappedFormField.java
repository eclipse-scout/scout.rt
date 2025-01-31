/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonWrappedFormField<WRAPPED_FORM_FIELD extends IWrappedFormField<? extends IForm>> extends JsonFormField<WRAPPED_FORM_FIELD> {

  public JsonWrappedFormField(WRAPPED_FORM_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WrappedFormField";
  }

  @Override
  protected void initJsonProperties(WRAPPED_FORM_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<WRAPPED_FORM_FIELD>(IWrappedFormField.PROP_INNER_FORM, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }

      @Override
      protected IForm modelValue() {
        return getModel().getInnerForm();
      }
    });
    putJsonProperty(new JsonProperty<WRAPPED_FORM_FIELD>(IWrappedFormField.PROP_INITIAL_FOCUS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInitialFocusEnabled();
      }
    });
  }
}
