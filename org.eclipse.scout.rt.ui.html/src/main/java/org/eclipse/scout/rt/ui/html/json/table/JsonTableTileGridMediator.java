/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRowTileMapping;
import org.eclipse.scout.rt.client.ui.basic.table.ITableTileGridMediator;
import org.eclipse.scout.rt.client.ui.tile.TileGridLayoutConfig;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.tile.JsonTileGridLayoutConfig;

public class JsonTableTileGridMediator<T extends ITableTileGridMediator> extends AbstractJsonPropertyObserver<T> {

  public JsonTableTileGridMediator(T model, IUiSession uiSession, String id, IJsonAdapter parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TableTileGridMediator";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<ITableTileGridMediator>(ITableTileGridMediator.PROP_TILE_MAPPINGS, model, getUiSession()) {
      @Override
      protected List<ITableRowTileMapping> modelValue() {
        return getModel().getTileMappings();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITableTileGridMediator.PROP_EXCLUSIVE_EXPAND, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isExclusiveExpand();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITableTileGridMediator.PROP_GRID_COLUMN_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getGridColumnCount();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITableTileGridMediator.PROP_WITH_PLACEHOLDERS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWithPlaceholders();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITableTileGridMediator.PROP_TILE_GRID_LAYOUT_CONFIG, model) {
      @Override
      protected TileGridLayoutConfig modelValue() {
        return getModel().getTileGridLayoutConfig();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonTileGridLayoutConfig((TileGridLayoutConfig) value).toJson();
      }
    });
  }
}
