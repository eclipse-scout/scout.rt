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
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.json.JSONObject;

public class JsonViewButton<VIEW_BUTTON extends IViewButton> extends JsonAction<VIEW_BUTTON> {

  public JsonViewButton(VIEW_BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
  }

  @Override
  protected void handleUiAction(JsonEvent event) {
    getModel().getUIFacade().setSelectedFromUI(true);
    super.handleUiAction(event);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("displayStyle", getModel().getDisplayStyle());
    return json;
  }

}
