/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

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
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.CLICKED.matches(event)) {
      handleUiClicked(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiClicked(JsonEvent event) {
    boolean uiChecked = event.getData().getBoolean("checked");
    addPropertyEventFilterCondition(IBooleanField.PROP_VALUE, uiChecked);
    getModel().getUIFacade().setCheckedFromUI(uiChecked);

    // In some cases the widget in the UI is clicked, which causes the check-box to be de-/selected, but the model rejects the value-change.
    // in that case we must "revert" the click in the UI, so that UI and model are in-sync again. This may happen, when the model-field throws
    // a VetoExeception in its execValidateValue() method.
    boolean modelChecked = getModel().isChecked();
    if (uiChecked != modelChecked) {
      addPropertyChangeEvent(IValueField.PROP_VALUE, modelChecked);
    }
  }
}
