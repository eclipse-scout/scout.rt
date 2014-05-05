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
package org.eclipse.scout.rt.ui.json.menu;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.JsonException;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonMenu extends AbstractJsonPropertyObserverRenderer<IMenu> {
  public static final String EVENT_MENU_ACTION = "menuAction";
  public static final String PROP_TEXT = IMenu.PROP_TEXT;
  public static final String PROP_ICON = IMenu.PROP_ICON_ID;

  public JsonMenu(IMenu modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
  }

  @Override
  public String getObjectType() {
    return "Menu";
  }

  @Override
  public JSONObject toJson() throws JsonException {
    JSONObject json = super.toJson();
    try {
      json.put(PROP_TEXT, getModelObject().getText());
      json.put(PROP_ICON, getModelObject().getIconId());//FIXME how to handle resources?
      return json;
    }
    catch (JSONException e) {
      throw new JsonException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonException {
    if (EVENT_MENU_ACTION.equals(event.getEventType())) {
      handleUiMenuAction(event, res);
    }
  }

  public void handleUiMenuAction(JsonEvent event, JsonResponse res) throws JsonException {
    new ClientSyncJob("Menu action", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getModelObject().getUIFacade().fireActionFromUI();
      }
    }.runNow(new NullProgressMonitor());
  }

}
