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
package org.eclipse.scout.rt.ui.html.json.form.fields.button;

import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonButton extends JsonFormField<IButton> {

  public final static String PROP_SYSTEM_TYPE = "systemType";

  public JsonButton(IButton model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void initJsonProperties(IButton model) {
    super.initJsonProperties(model);

    // TODO AWE: System-type von button mit ans UI schicken?
    putJsonProperty(new JsonProperty<IButton>(PROP_SYSTEM_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSystemType();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "Button";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.CLICK.matches(event)) {
      getModel().getUIFacade().fireButtonClickedFromUI();
    }
    else {
      super.handleUiEvent(event, res);
    }
  }
}
