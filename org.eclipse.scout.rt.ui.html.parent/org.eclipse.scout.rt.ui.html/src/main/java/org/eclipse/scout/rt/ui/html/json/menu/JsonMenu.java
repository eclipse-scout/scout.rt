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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.json.JSONObject;

public class JsonMenu extends AbstractJsonPropertyObserver<IMenu> {

  public static final String EVENT_MENU_ACTION = "menuAction";
  public static final String EVENT_ABOUT_TO_SHOW = "aboutToShow";
  public static final String PROP_SEPARATOR = "separator";
  public static final String PROP_MENU_TYPES = "menuTypes";
  public static final String PROP_CHILD_MENUS = "childMenus";

  public JsonMenu(IMenu model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<IMenu, String>(IMenu.PROP_TEXT, model) {
      @Override
      protected String getValueImpl(IMenu modelObject) {
        return modelObject.getText();
      }
    });
    putJsonProperty(new JsonProperty<IMenu, String>(IMenu.PROP_ICON_ID, model) {
      @Override
      protected String getValueImpl(IMenu modelObject) {
        return modelObject.getIconId();//FIXME CGU how to handle resources?
      }
    });

    putJsonProperty(new JsonProperty<IMenu, Boolean>(IMenu.PROP_ENABLED, model) {
      @Override
      protected Boolean getValueImpl(IMenu modelObject) {
        return modelObject.isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IMenu, Boolean>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean getValueImpl(IMenu modelObject) {
        return modelObject.isVisible();
      }
    });
    putJsonProperty(new JsonProperty<IMenu, Boolean>(PROP_SEPARATOR, model) {
      @Override
      protected Boolean getValueImpl(IMenu modelObject) {
        return modelObject.isSeparator();
      }
    });
    putJsonProperty(new JsonProperty<IMenu, Set<String>>(PROP_MENU_TYPES, model) {
      @Override
      protected Set<String> getValueImpl(IMenu modelObject) {
        Set<String> menuTypes = new HashSet<>();
        for (IMenuType type : modelObject.getMenuTypes()) {
          menuTypes.add(type.toString());
        }
        return menuTypes;
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, PROP_CHILD_MENUS, modelObjectsToJson(getModelObject().getChildActions()));
    return json;
  }

  @Override
  public String getObjectType() {
    return "Menu";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_MENU_ACTION.equals(event.getType())) {
      handleUiMenuAction(event, res);
    }
    else if (EVENT_ABOUT_TO_SHOW.equals(event.getType())) {
      handleUiMenuAboutToShow(event, res);
    }
  }

  public void handleUiMenuAction(JsonEvent event, JsonResponse res) {
    getModelObject().getUIFacade().fireActionFromUI();
  }

  public void handleUiMenuAboutToShow(JsonEvent event, JsonResponse res) {
    getModelObject().aboutToShow();
  }
}
