/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton;

import org.eclipse.scout.rt.client.ui.form.fields.button.IButtonUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.json.JSONObject;

public class JsonRadioButton<RADIO_BUTTON extends IRadioButton> extends JsonButton<RADIO_BUTTON> {

  private static final String SELECTED = "selected";

  public JsonRadioButton(RADIO_BUTTON model, IUiSession uiSession, String id, IJsonAdapter parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButton";
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (SELECTED.equals(propertyName)) {
      boolean selected = data.getBoolean(IRadioButton.PROP_SELECTED);
      addPropertyEventFilterCondition(IRadioButton.PROP_SELECTED, selected);
      IButtonUIFacade uiFacade = getModel().getUIFacade();
      uiFacade.setSelectedFromUI(selected);
      if (selected) {
        uiFacade.fireButtonClickFromUI();
      }
    }
  }
}
