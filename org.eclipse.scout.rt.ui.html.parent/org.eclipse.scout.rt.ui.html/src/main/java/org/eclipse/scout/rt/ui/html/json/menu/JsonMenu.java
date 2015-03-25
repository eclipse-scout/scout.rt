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
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.json.JSONObject;

public class JsonMenu<T extends IMenu> extends JsonAction<T> {

  public static final String EVENT_ABOUT_TO_SHOW = "aboutToShow";
  public static final String PROP_SEPARATOR = "separator";
  public static final String PROP_CHILD_MENUS = "childMenus";
  public static final String PROP_SYSTEM_TYPE = "systemType";

  public JsonMenu(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<IMenu>(PROP_SEPARATOR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSeparator();
      }
    });
    putJsonProperty(new JsonProperty<IMenu>(IMenu.PROP_MENU_TYPES, model) {
      @Override
      protected Set<IMenuType> modelValue() {
        return getModel().getMenuTypes();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        Set<String> menuTypes = new HashSet<>();
        for (IMenuType type : getModel().getMenuTypes()) {
          String prefix = type.getClass().getSimpleName().replace("MenuType", "");
          menuTypes.add(prefix + "." + type.toString());
        }
        return menuTypes;
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getChildActions());
  }

  @Override
  // FIXME: AWE gucken ob das noch funktioniert wenn wir das löschen
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (IMenu.PROP_MENU_TYPES.equals(propertyName)) {
      JsonProperty jsonProperty = getJsonProperty(IMenu.PROP_MENU_TYPES);
      addPropertyChangeEvent(propertyName, jsonProperty.prepareValueForToJson(newValue));
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), PROP_CHILD_MENUS, getModel().getChildActions());
  }

  @Override
  public String getObjectType() {
    return "Menu";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_ABOUT_TO_SHOW.equals(event.getType())) {
      handleUiMenuAboutToShow(event, res);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  public void handleUiMenuAboutToShow(JsonEvent event, JsonResponse res) {
    getModel().aboutToShow();
  }
}
