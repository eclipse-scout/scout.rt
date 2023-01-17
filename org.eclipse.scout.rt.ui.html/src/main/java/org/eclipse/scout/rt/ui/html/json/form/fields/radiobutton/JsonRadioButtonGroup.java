/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonLogicalGridLayoutConfig;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonRadioButtonGroup<RADIO_BUTTON_GROUP extends IRadioButtonGroup<?>> extends JsonValueField<RADIO_BUTTON_GROUP> {

  private static final String FIELDS = "fields";

  public JsonRadioButtonGroup(RADIO_BUTTON_GROUP model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButtonGroup";
  }

  @Override
  protected void initJsonProperties(RADIO_BUTTON_GROUP model) {
    super.initJsonProperties(model);
    removeJsonProperty(IValueField.PROP_DISPLAY_TEXT);
    putJsonProperty(new JsonProperty<RADIO_BUTTON_GROUP>(IRadioButtonGroup.PROP_GRID_COLUMN_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getGridColumnCount();
      }
    });
    putJsonProperty(new JsonProperty<RADIO_BUTTON_GROUP>(IRadioButtonGroup.PROP_LAYOUT_CONFIG, model) {
      @Override
      protected LogicalGridLayoutConfig modelValue() {
        return getModel().getLayoutConfig();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonLogicalGridLayoutConfig((LogicalGridLayoutConfig) value).toJson();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getFields());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), FIELDS, getModel().getFields());
  }
}
