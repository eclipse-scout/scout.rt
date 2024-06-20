/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.tile.IFormFieldTile;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.data.colorscheme.IColorScheme;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

/**
 * @since 8.0
 */
public class JsonTile<T extends ITile> extends AbstractJsonWidget<T> {

  public JsonTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Tile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ITile>(ITile.PROP_COLOR_SCHEME, model) {
      @Override
      protected IColorScheme modelValue() {
        return getModel().getColorScheme();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return ((IColorScheme) value).getIdentifier();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormFieldTile.PROP_DISPLAY_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITile.PROP_LOADING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoading();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITile.PROP_GRID_DATA_HINTS, model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridDataHints();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonGridData.toJson((GridData) value);
      }
    });
  }

}
