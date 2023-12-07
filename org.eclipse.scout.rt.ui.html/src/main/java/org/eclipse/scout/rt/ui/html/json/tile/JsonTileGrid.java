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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.client.ui.tile.TileGridLayoutConfig;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 8.0
 */
public class JsonTileGrid<T extends ITileGrid<? extends ITile>> extends AbstractJsonWidget<T> implements IJsonContextMenuOwner {
  private static final Logger LOG = LoggerFactory.getLogger(JsonTileGrid.class);
  public static final String EVENT_TILE_CLICK = "tileClick";
  public static final String EVENT_TILE_ACTION = "tileAction";

  private JsonContextMenu<IContextMenu> m_jsonContextMenu;

  public JsonTileGrid(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TileGrid";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    m_jsonContextMenu = new JsonContextMenu<>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  @Override
  protected void disposeChildAdapters() {
    getJsonContextMenu().dispose();
    super.disposeChildAdapters();
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<>(ITileGrid.PROP_TILES, model, getUiSession()) {
      @Override
      protected List<? extends ITile> modelValue() {
        return getModel().getTiles();
      }
    });
    putJsonProperty(new JsonAdapterProperty<>(ITileGrid.PROP_SELECTED_TILES, model, getUiSession()) {
      @Override
      protected List<? extends ITile> modelValue() {
        return getModel().getSelectedTiles();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
    putJsonProperty(new JsonAdapterProperty<>(ITileGrid.PROP_FILTERED_TILES, model, getUiSession()) {
      @Override
      protected List<? extends ITile> modelValue() {
        return getModel().getFilteredTiles();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (getModel().getFilters().size() == 0) {
          // If no filter is active return null instead of an array which contains the same content as the tiles array
          return null;
        }
        return super.prepareValueForToJson(value);
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
    putJsonProperty(new JsonProperty<ITileGrid>(ITileGrid.PROP_GRID_COLUMN_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getGridColumnCount();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_SELECTABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelectable();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_MULTI_SELECT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultiSelect();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_WITH_PLACEHOLDERS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWithPlaceholders();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_LOGICAL_GRID, model) {
      @Override
      protected String modelValue() {
        return getModel().getLogicalGrid();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_LAYOUT_CONFIG, model) {
      @Override
      protected TileGridLayoutConfig modelValue() {
        return getModel().getLayoutConfig();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonTileGridLayoutConfig((TileGridLayoutConfig) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_VIRTUAL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVirtual();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_ANIMATE_TILE_REMOVAL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAnimateTileRemoval();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_ANIMATE_TILE_INSERTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAnimateTileInsertion();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_TEXT_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTextFilterEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(ITileGrid.PROP_WRAPPABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWrappable();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(PROP_MENUS, getJsonContextMenu().childActionsToJson());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_TILE_CLICK.equals(event.getType())) {
      handleUiTileClick(event);
    }
    else if (EVENT_TILE_ACTION.equals(event.getType())) {
      handleUiTileAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ITileGrid.PROP_SELECTED_TILES.equals(propertyName)) {
      handleUiSelectedTiles(data);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  @SuppressWarnings("unchecked")
  protected void handleUiSelectedTiles(JSONObject data) {
    JSONArray tileIds = data.getJSONArray(ITileGrid.PROP_SELECTED_TILES);
    List<ITile> tiles = extractTiles(tileIds);
    if (tiles.isEmpty() && tileIds.length() > 0) {
      // Ignore inconsistent selections from UI (probably an obsolete cached event)
      return;
    }
    if (tiles.size() == tileIds.length()) {
      addPropertyEventFilterCondition(ITileGrid.PROP_SELECTED_TILES, tiles);
    }
    getModel().getUIFacade().setSelectedTilesFromUI(tiles);
  }

  protected List<ITile> extractTiles(JSONArray tileIds) {
    List<ITile> tiles = new ArrayList<>(tileIds.length());
    for (int i = 0; i < tileIds.length(); i++) {
      ITile tile = optTile(tileIds.getString(i));
      if (tile != null) {
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @SuppressWarnings("unchecked")
  protected void handleUiTileClick(JsonEvent event) {
    ITile tile = extractTile(event.getData());
    if (tile == null) {
      LOG.info("Requested tile doesn't exist anymore -> skip tileClick event");
      return;
    }
    MouseButton mouseButton = extractMouseButton(event.getData());
    getModel().getUIFacade().handleTileClickFromUI(tile, mouseButton);
  }

  protected MouseButton extractMouseButton(JSONObject json) {
    int mouseButton = json.getInt("mouseButton");
    switch (mouseButton) {
      case 1:
        return MouseButton.Left;
      case 3:
        return MouseButton.Right;
      default:
        return MouseButton.Unknown;
    }
  }

  @SuppressWarnings("unchecked")
  protected void handleUiTileAction(JsonEvent event) {
    ITile tile = extractTile(event.getData());
    if (tile == null) {
      LOG.info("Requested tile doesn't exist anymore -> skip tileAction event");
      return;
    }
    getModel().getUIFacade().handleTileActionFromUI(tile);
  }

  protected ITile extractTile(JSONObject json) {
    return optTile(json.getString("tile"));
  }

  protected ITile getTile(String tileId) {
    Object model = getUiSession().getJsonAdapter(tileId).getModel();
    if (!(model instanceof ITile)) {
      throw new IllegalStateException("Id does not belong to a tile. Id: " + tileId);
    }
    return (ITile) model;
  }

  protected ITile optTile(String tileId) {
    IJsonAdapter<?> adapter = getUiSession().getJsonAdapter(tileId);
    if (adapter == null) {
      return null;
    }
    Object model = adapter.getModel();
    if (model == null) {
      return null;
    }
    if (!(model instanceof ITile)) {
      throw new IllegalStateException("Id does not belong to a tile. Id: " + tileId);
    }
    return (ITile) model;
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  public JsonContextMenu<IContextMenu> getJsonContextMenu() {
    return m_jsonContextMenu;
  }
}
