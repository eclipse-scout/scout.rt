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
package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.menus.TableOrganizeMenu;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.json.JSONObject;

public class JsonTableOrganizeMenu extends JsonMenu<TableOrganizeMenu> {

  public JsonTableOrganizeMenu(TableOrganizeMenu model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TableOrganizeMenu";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, "development", getModel().isDevelopment());
    putProperty(json, "columnsCustomizable", getModel().isColumnsCustomizable());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.CLICKED.matches(event)) {
      getModel().getUIFacade().fireActionFromUI();
    }
    else {
      super.handleUiEvent(event);
    }
  }

}
