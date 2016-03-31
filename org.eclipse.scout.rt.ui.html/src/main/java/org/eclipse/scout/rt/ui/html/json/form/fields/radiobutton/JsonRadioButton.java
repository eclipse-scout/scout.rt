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
package org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton;

import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;

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
  public void handleUiEvent(JsonEvent event) {
    if (SELECTED.equals(event.getType())) {
      handleUiSelected(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSelected(JsonEvent event) {
    boolean selected = event.getData().getBoolean(IRadioButton.PROP_SELECTED);
    addPropertyEventFilterCondition(IRadioButton.PROP_SELECTED, selected);
    getModel().getUIFacade().setSelectedFromUI(selected);
    if (selected) {
      getModel().getUIFacade().fireButtonClickedFromUI();
    }
  }
}
