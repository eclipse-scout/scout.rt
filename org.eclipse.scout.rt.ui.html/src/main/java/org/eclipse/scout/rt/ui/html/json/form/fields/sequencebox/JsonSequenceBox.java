/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.sequencebox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonLogicalGridLayoutConfig;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonCompositeField;

/**
 * @param <T>
 *     Model of SequenceBox
 */
public class JsonSequenceBox<T extends ISequenceBox> extends JsonCompositeField<T, IFormField> {

  public JsonSequenceBox(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SequenceBox";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IRadioButtonGroup.PROP_LAYOUT_CONFIG, model) {
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
}
