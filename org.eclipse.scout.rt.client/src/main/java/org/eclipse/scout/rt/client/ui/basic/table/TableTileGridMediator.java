/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.tile.TileGridLayoutConfig;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

public class TableTileGridMediator extends AbstractPropertyObserver implements ITableTileGridMediator, PropertyChangeListener, TableListener {

  protected ITable m_table;

  public TableTileGridMediator(ITable table) {
    this(table, true);
  }

  public TableTileGridMediator(ITable table, boolean callInitializer) {
    m_table = table;
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    initConfig();
  }

  protected void initConfig() {
    m_table.addPropertyChangeListener(ITable.PROP_TILE_MODE, this);
    m_table.addTableListener(this);

    setExclusiveExpand(getConfiguredExclusiveExpand());
    setGridColumnCount(getConfiguredGridColumnCount());
    setWithPlaceholders(getConfiguredWithPlaceholders());
    setTileGridLayoutConfig(getConfiguredLayoutConfig());
  }

  protected boolean getConfiguredExclusiveExpand() {
    return false;
  }

  protected int getConfiguredGridColumnCount() {
    return 4;
  }

  protected boolean getConfiguredWithPlaceholders() {
    return false;
  }

  protected TileGridLayoutConfig getConfiguredLayoutConfig() {
    return new TileGridLayoutConfig();
  }

  @Override
  public void tableChanged(TableEvent e) {
    if (m_table.isTileMode()) {
      List<ITableRowTileMapping> tileMappings = new ArrayList<>(getTileMappings());
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_INSERTED:
          tileMappings.addAll(m_table.createTiles(e.getRows()));
          setTileMappings(tileMappings);
          break;
        case TableEvent.TYPE_ROWS_DELETED:
          tileMappings.removeIf(m -> e.getRows().contains(m.getTableRow()));
          setTileMappings(tileMappings);
          break;
        case TableEvent.TYPE_ALL_ROWS_DELETED:
          setTileMappings(new ArrayList<>());
          break;
      }
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (m_table.isTileMode()) {
      loadTiles(m_table.getRows());
    }
  }

  protected void loadTiles(List<ITableRow> rows) {
    setTileMappings(m_table.createTiles(rows));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ITableRowTileMapping> getTileMappings() {
    return (List<ITableRowTileMapping>) propertySupport.getProperty(PROP_TILE_MAPPINGS);
  }

  @Override
  public void setTileMappings(List<ITableRowTileMapping> tiles) {
    propertySupport.setProperty(PROP_TILE_MAPPINGS, tiles);
  }

  /**
   * The following properties are to control the mediator's internal tileAccordion (only available in the ui)
   */
  @Override
  public boolean isExclusiveExpand() {
    return propertySupport.getPropertyBool(PROP_EXCLUSIVE_EXPAND);
  }

  @Override
  public void setExclusiveExpand(boolean exclusiveExpand) {
    propertySupport.setPropertyBool(PROP_EXCLUSIVE_EXPAND, exclusiveExpand);
  }

  @Override
  public void setGridColumnCount(int gridColumnCount) {
    propertySupport.setPropertyInt(PROP_GRID_COLUMN_COUNT, gridColumnCount);
  }

  @Override
  public int getGridColumnCount() {
    return propertySupport.getPropertyInt(PROP_GRID_COLUMN_COUNT);
  }

  @Override
  public void setWithPlaceholders(boolean withPlaceholders) {
    propertySupport.setPropertyBool(PROP_WITH_PLACEHOLDERS, withPlaceholders);
  }

  @Override
  public boolean isWithPlaceholders() {
    return propertySupport.getPropertyBool(PROP_WITH_PLACEHOLDERS);
  }

  @Override
  public void setTileGridLayoutConfig(TileGridLayoutConfig layoutConfig) {
    propertySupport.setProperty(PROP_TILE_GRID_LAYOUT_CONFIG, layoutConfig);
  }

  @Override
  public TileGridLayoutConfig getTileGridLayoutConfig() {
    return (TileGridLayoutConfig) propertySupport.getProperty(PROP_TILE_GRID_LAYOUT_CONFIG);
  }
}
