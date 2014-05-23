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
package org.eclipse.scout.rt.ui.html.json.menu;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.json.JSONObject;

public class JsonMenu extends AbstractJsonPropertyObserverRenderer<IMenu> {

  public static final String EVENT_MENU_ACTION = "menuAction";
  public static final String PROP_TEXT = IMenu.PROP_TEXT;
  public static final String PROP_ICON = IMenu.PROP_ICON_ID;

  public JsonMenu(IMenu modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Menu";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, PROP_TEXT, getModelObject().getText());
    putProperty(json, PROP_ICON, getModelObject().getIconId());//FIXME how to handle resources?
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_MENU_ACTION.equals(event.getType())) {
      handleUiMenuAction(event, res);
    }
  }

  public void handleUiMenuAction(JsonEvent event, JsonResponse res) {
    getModelObject().getUIFacade().fireActionFromUI();
  }

}
