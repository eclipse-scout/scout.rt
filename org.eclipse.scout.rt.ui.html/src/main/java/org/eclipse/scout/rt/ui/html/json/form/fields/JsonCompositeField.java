/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.json.JSONObject;

public class JsonCompositeField<COMPOSITE_FIELD extends ICompositeField, F extends IFormField> extends JsonFormField<COMPOSITE_FIELD> {

  public JsonCompositeField(COMPOSITE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CompositeField";
  }

  @Override
  protected void initJsonProperties(COMPOSITE_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<COMPOSITE_FIELD>(ICompositeField.PROP_FIELDS, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableFormFieldFilter<>()).build();
      }

      @Override
      protected List<IFormField> modelValue() {
        return getModel().getFields();
      }

      @Override
      public String jsonPropertyName() {
        return getModelFieldsPropertyName();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), getModelFieldsPropertyName(), getModelFields(), new DisplayableFormFieldFilter<>());
  }

  @SuppressWarnings("unchecked")
  protected List<F> getModelFields() {
    return (List<F>) getModel().getFields();
  }

  protected String getModelFieldsPropertyName() {
    return "fields";
  }

}
