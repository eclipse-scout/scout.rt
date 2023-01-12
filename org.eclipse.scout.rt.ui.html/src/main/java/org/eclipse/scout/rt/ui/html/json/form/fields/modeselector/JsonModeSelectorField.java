/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.modeselector;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;
import org.eclipse.scout.rt.client.ui.form.fields.modeselector.IModeSelectorField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonModeSelectorField<T extends IModeSelectorField<?>> extends JsonValueField<T> {

  public JsonModeSelectorField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ModeSelectorField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IModeSelectorField.PROP_MODES, model, getUiSession()) {
      @Override
      protected List<? extends IMode<?>> modelValue() {
        return getModel().getModes();
      }
    });
  }
}
