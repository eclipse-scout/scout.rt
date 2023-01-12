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

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonBasicField<T extends IBasicField<?>> extends JsonValueField<T> {

  public JsonBasicField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBasicField<?>>(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUpdateDisplayTextOnModify();
      }
    });
    putJsonProperty(new JsonProperty<IBasicField<?>>(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY_DELAY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getUpdateDisplayTextOnModifyDelay();
      }
    });
  }
}
