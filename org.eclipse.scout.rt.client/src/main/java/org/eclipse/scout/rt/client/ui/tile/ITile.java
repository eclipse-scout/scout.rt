package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.shared.data.tile.ITileColorScheme;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * @since 7.1
 */
public interface ITile extends IWidget, IOrdered, IStyleable, IExtensibleObject, ITypeWithClassId {
  String PROP_ORDER = "order";
  String PROP_COLOR_SCHEME = "colorScheme";
  String PROP_GRID_DATA_HINTS = "gridDataHints";
  String PROP_LOADING = "loading";

  ITileColorScheme getColorScheme();

  void setColorScheme(ITileColorScheme colorScheme);

  /**
   * @return the grid data hints used by the logical grids to create the final grid data
   */
  GridData getGridDataHints();

  void setGridDataHints(GridData data);

  void init();

  void postInitConfig();

  void dispose();

  ITiles getContainer();

  void setContainer(ITiles container);

  void ensureDataLoaded();

  void loadData();

  void onLoadDataCancel();

  void setLoading(boolean loading);

  boolean isLoading();
}
