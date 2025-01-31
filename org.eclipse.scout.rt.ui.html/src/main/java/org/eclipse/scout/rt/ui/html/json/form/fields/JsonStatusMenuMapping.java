/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IStatusMenuMapping;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.json.JSONArray;

public class JsonStatusMenuMapping<T extends IStatusMenuMapping> extends AbstractJsonPropertyObserver<T> {

  public JsonStatusMenuMapping(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "StatusMenuMapping";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IStatusMenuMapping.PROP_CODES, model) {
      @Override
      protected List<Integer> modelValue() {
        return getModel().getCodes();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return new JSONArray((List<Integer>) value); // Do NOT remove the cast! It is required to use the correct constructor.
      }
    });
    putJsonProperty(new JsonProperty<T>(IStatusMenuMapping.PROP_SEVERITIES, model) {
      @Override
      protected List<Integer> modelValue() {
        return getModel().getSeverities();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return new JSONArray((List<Integer>) value); // Do NOT remove the cast! It is required to use the correct constructor.
      }
    });
    putJsonProperty(new JsonProperty<T>(IStatusMenuMapping.PROP_MENU, model) {

      @Override
      protected IMenu modelValue() {
        return getModel().getMenu();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        // The menu is managed by the parent -> don't create a new adapter but use the one from the parent instead
        IJsonAdapter<IMenu> adapter = getParent().getAdapter((IMenu) value, new DisplayableActionFilter<>());
        if (adapter == null) {
          return null;
        }
        return adapter.getId();
      }
    });
  }
}
