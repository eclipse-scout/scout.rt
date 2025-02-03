/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.util.Optional;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;

public class JsonViewButton<VIEW_BUTTON extends IViewButton> extends JsonAction<VIEW_BUTTON> {

  public JsonViewButton(VIEW_BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
  }

  @Override
  protected void initJsonProperties(VIEW_BUTTON model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<VIEW_BUTTON>(IViewButton.PROP_DISPLAY_STYLE, model) {
      @Override
      protected String modelValue() {
        return Optional.ofNullable(getModel().getDisplayStyle()).map(Enum::toString).orElse(null);
      }
    });
  }

  @Override
  protected void handleUiAction(JsonEvent event) {
    getModel().getUIFacade().setSelectedFromUI(true);
    super.handleUiAction(event);
  }
}
