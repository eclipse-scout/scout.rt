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
package org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton;

import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;

public class JsonRadioButton<T extends IRadioButton> extends JsonButton<T> {

  private static final String SELECTED = "selected";

  public JsonRadioButton(T model, IJsonSession jsonSession, String id, IJsonAdapter parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButton";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IRadioButton>(IRadioButton.PROP_RADIOVALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getRadioValue();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (SELECTED.equals(event.getType())) {
      addPropertyEventFilterCondition(IRadioButton.PROP_SELECTED, true);
      getModel().getUIFacade().fireButtonClickedFromUI();
      getModel().getUIFacade().setSelectedFromUI(true);
    }
    else {
      super.handleUiEvent(event);
    }
  }
}
