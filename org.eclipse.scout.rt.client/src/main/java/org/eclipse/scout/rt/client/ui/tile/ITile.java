/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeObserver;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile.ITileDataLoader;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.shared.data.colorscheme.IColorScheme;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * @since 8.0
 */
public interface ITile extends IWidget, IOrdered, IStyleable, IExtensibleObject, IDataChangeObserver, ITileLoadCancellable {
  String PROP_ORDER = "order";
  String PROP_COLOR_SCHEME = "colorScheme";
  String PROP_GRID_DATA_HINTS = "gridDataHints";
  String PROP_DISPLAY_STYLE = "displayStyle";

  /**
   * This is the default display style. If it is active, default styling is applied like visualizing the selection.
   */
  String DISPLAY_STYLE_DEFAULT = "default";

  /**
   * The plain style tries to render the tile as it is without adjusting the look or behavior. This gives you an easy
   * possibility to style it as you like.
   */
  String DISPLAY_STYLE_PLAIN = "plain";

  String getDisplayStyle();

  IColorScheme getColorScheme();

  void setColorScheme(IColorScheme colorScheme);

  /**
   * @return the grid data hints used by the logical grids to create the final grid data
   */
  GridData getGridDataHints();

  void setGridDataHints(GridData data);

  void setFilterAccepted(boolean filterAccepted);

  boolean isFilterAccepted();

  void ensureDataLoaded();

  /**
   * Loads the data using {@link ITileDataLoader}. Does nothing if the data is already being loaded.
   */
  void loadData();

  /**
   * Calls {@link #cancelLoading()} and {@link #loadData()}.
   */
  void reloadData();

  /**
   * Cancels data loading if loading is in progress.
   */
  void cancelLoading();

  @Override
  void setLoading(boolean loading);

  @Override
  boolean isLoading();
}
