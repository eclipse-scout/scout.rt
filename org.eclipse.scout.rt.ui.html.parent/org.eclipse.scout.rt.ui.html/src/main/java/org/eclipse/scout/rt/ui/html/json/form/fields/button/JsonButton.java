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
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonButton<T extends IButton> extends JsonFormField<T> {

  public static final String PROP_SYSTEM_TYPE = "systemType";
  public static final String PROP_PROCESS_BUTTON = "processButton";
  public static final String PROP_DISPLAY_STYLE = "displayStyle";

  public JsonButton(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IButton>(PROP_SYSTEM_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSystemType();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_PROCESS_BUTTON, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isProcessButton();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_DISPLAY_STYLE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "Button";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.CLICKED.matches(event)) {
      getModel().getUIFacade().fireButtonClickedFromUI();
    }
    else {
      super.handleUiEvent(event, res);
    }
  }
}
