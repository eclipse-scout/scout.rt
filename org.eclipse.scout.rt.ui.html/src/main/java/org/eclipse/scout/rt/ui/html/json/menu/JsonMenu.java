/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.menu;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineMenuWrapper;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.json.JSONArray;

public class JsonMenu<MENU extends IMenu> extends JsonAction<MENU> {

  public static final String PROP_SEPARATOR = "separator";
  public static final String PROP_OUTLINE_MENU_WRAPPER = "outlineMenuWrapper";

  public JsonMenu(MENU model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Menu";
  }

  @Override
  protected void initJsonProperties(MENU model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<MENU>(PROP_SEPARATOR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSeparator();
      }
    });
    putJsonProperty(new JsonProperty<MENU>(PROP_OUTLINE_MENU_WRAPPER, model) {
      @Override
      protected Boolean modelValue() {
        return getModel() instanceof OutlineMenuWrapper;
      }
    });
    putJsonProperty(new JsonProperty<MENU>(IMenu.PROP_MENU_TYPES, model) {
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
        return new JSONArray(menuTypes);
      }
    });
    putJsonProperty(new JsonProperty<MENU>(IMenu.PROP_PREVENT_DOUBLE_CLICK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isPreventDoubleClick();
      }
    });
    putJsonProperty(new JsonProperty<MENU>(IMenu.PROP_STACKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStackable();
      }
    });
    putJsonProperty(new JsonProperty<MENU>(IMenu.PROP_SHRINKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isShrinkable();
      }
    });
    putJsonProperty(new JsonProperty<MENU>(IMenu.PROP_SUB_MENU_VISIBILITY, model) {
      @Override
      protected String modelValue() {
        return getModel().getSubMenuVisibility();
      }
    });
  }
}
