/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.mode;

import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;

public class JsonMode<MODE extends IMode<?>> extends JsonAction<MODE> {

  public JsonMode(MODE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Mode";
  }

  @Override
  protected void initJsonProperties(MODE model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<MODE>(IMode.PROP_REF, model) {
      @Override
      protected Object modelValue() {
        return getModel().getRef();
      }
    });
  }
}
