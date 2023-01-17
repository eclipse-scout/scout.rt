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

import org.eclipse.scout.rt.client.ui.form.fields.button.IButtonUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.json.JSONObject;

public class JsonRadioButton<RADIO_BUTTON extends IRadioButton<?>> extends JsonButton<RADIO_BUTTON> {

  private static final String SELECTED = "selected";

  public JsonRadioButton(RADIO_BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButton";
  }

  @Override
  protected void initJsonProperties(RADIO_BUTTON model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IRadioButton<?>>(IRadioButton.PROP_WRAP_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWrapText();
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (SELECTED.equals(propertyName)) {
      boolean selected = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, selected);
      IButtonUIFacade uiFacade = getModel().getUIFacade();
      uiFacade.setSelectedFromUI(selected);
      if (selected) {
        uiFacade.fireButtonClickFromUI();
      }
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
