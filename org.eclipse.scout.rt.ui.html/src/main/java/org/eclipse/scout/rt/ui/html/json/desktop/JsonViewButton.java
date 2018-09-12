/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
        return Optional.ofNullable(getModel().getDisplayStyle()).map(displayStyle -> displayStyle.toString()).orElse(null);
      }
    });
  }

  @Override
  protected void handleUiAction(JsonEvent event) {
    getModel().getUIFacade().setSelectedFromUI(true);
    super.handleUiAction(event);
  }

}
