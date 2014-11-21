/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

/**
 * This class creates JSON output for an IBooleanField used as a check-box.
 */
public class JsonCheckBoxField<T extends IBooleanField> extends JsonValueField<T> {

  public JsonCheckBoxField(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "CheckBoxField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBooleanField>(IBooleanField.PROP_VALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getValue();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.CLICK.matches(event)) {
      handleUiClick(event, JsonObjectUtility.getBoolean(event.getData(), "checked"));
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  private void handleUiClick(JsonEvent event, boolean uiChecked) {
    boolean modelChecked = getModel().isChecked();
    if (uiChecked != modelChecked) {
      getModel().setChecked(uiChecked);
      /* In some cases the widget in the UI is clicked, which causes the check-box to be de-/selected, but the model rejects the value-change.
       * in that case we must "revert" the click in the UI, so that UI and model are in-sync again. This may happen, when the model-field throws
       * a VetoExeception in its execValidateValue() method.
       */
      modelChecked = getModel().isChecked();
      if (uiChecked != modelChecked) {
        addPropertyChangeEvent(IValueField.PROP_VALUE, modelChecked);
      }
    }

  }

}
