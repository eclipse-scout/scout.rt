/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
