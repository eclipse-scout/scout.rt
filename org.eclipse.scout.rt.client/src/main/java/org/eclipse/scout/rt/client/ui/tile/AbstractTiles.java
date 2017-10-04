package org.eclipse.scout.rt.client.ui.tile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * @since 7.1
 */
@ClassId("c04e6cf7-fda0-4146-afea-6a0ff0a50c4b")
public abstract class AbstractTiles extends AbstractWidget implements ITiles {
  private boolean m_initialized;

  public AbstractTiles() {
    this(true);
  }

  public AbstractTiles(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void callInitializer() {
    if (isInitialized()) {
      return;
    }
    super.callInitializer();
    setInitialized(true);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setGridColumnCount(getConfiguredGridColumnCount());
    setLogicalGrid(getConfiguredLogicalGrid());
    setLogicalGridColumnWidth(getConfiguredLogicalGridColumnWidth());
    setLogicalGridRowHeight(getConfiguredLogicalGridRowHeight());
    setLogicalGridHGap(getConfiguredLogicalGridHGap());
    setLogicalGridVGap(getConfiguredLogicalGridVGap());
    // getConfiguredMaxContentWidth should not be moved up so that calculatePreferredWidth may be used inside getConfiguredMaxContentWidth()
    setMaxContentWidth(getConfiguredMaxContentWidth());
    setScrollable(getConfiguredScrollable());
    setWithPlaceholders(getConfiguredWithPlaceholders());

    OrderedCollection<ITile> tiles = new OrderedCollection<>();
    injectTilesInternal(tiles);
    setTiles(tiles.getOrderedList());
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  protected void setInitialized(boolean initialized) {
    m_initialized = initialized;
  }

  protected void injectTilesInternal(OrderedCollection<ITile> tiles) {
    List<Class<? extends ITile>> tileClasses = getConfiguredTiles();
    for (Class<? extends ITile> tileClass : tileClasses) {
      ITile tile = createTileInternal(tileClass);
      if (tile != null) {
        tiles.addOrdered(tile);
      }
    }
  }

  protected ITile createTileInternal(Class<? extends ITile> tileClass) {
    return ConfigurationUtility.newInnerInstance(this, tileClass);
  }

  protected List<Class<? extends ITile>> getConfiguredTiles() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITile>> filtered = ConfigurationUtility.filterClasses(dca, ITile.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public void initTiles() {
    for (ITile tile : getTiles()) {
      tile.init();
    }
  }

  @Override
  public void postInitTilesConfig() {
    for (ITile tile : getTiles()) {
      tile.postInitConfig();
    }
  }

  @Override
  public void disposeTiles() {
    for (ITile tile : getTiles()) {
      tile.dispose();
    }
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(5)
  protected int getConfiguredGridColumnCount() {
    return 4;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(8)
  protected String getConfiguredLogicalGrid() {
    return LOGICAL_GRID_HORIZONTAL;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(10)
  protected int getConfiguredLogicalGridColumnWidth() {
    return 200;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(15)
  protected int getConfiguredLogicalGridRowHeight() {
    return 150;
  }

  /**
   * Configures the gap between two logical grid columns.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredLogicalGridHGap() {
    return 15;
  }

  /**
   * Configures the gap between two logical grid rows.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(25)
  protected int getConfiguredLogicalGridVGap() {
    return 20;
  }

  /**
   * Configures the maximum width in pixels to use for the content. The maximum is disabled if this value is
   * <code>&lt;= 0</code>.
   * <p>
   * You may use {@link #calculatePreferredWidth()} if you want to limit the width based on the column count, column
   * width and column gap.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(290)
  protected int getConfiguredMaxContentWidth() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredWithPlaceholders() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredScrollable() {
    return true;
  }

  @Override
  public List<? extends ITile> getTiles() {
    return propertySupport.getPropertyList(PROP_TILES);
  }

  @Override
  public void setTiles(List<? extends ITile> tiles) {
    List<? extends ITile> oldTiles = ObjectUtility.nvl(getTiles(), new ArrayList<>());
    List<? extends ITile> newTiles = ObjectUtility.nvl(tiles, new ArrayList<>());

    // Dispose old tiles (only if they are not in the new list)
    for (ITile tile : oldTiles) {
      if (!newTiles.contains(tile)) {
        tile.dispose();
      }
    }

    propertySupport.setPropertyList(PROP_TILES, tiles);

    // Initialize new tiles
    // Only initialize when tiles are added later,
    // if they are added while initConfig runs, initTiles() will take care of the initialization which will be called by the container (e.g. TilesField)
    for (ITile tile : newTiles) {
      tile.setContainer(this);
      if (isInitialized() && !oldTiles.contains(tile)) {
        tile.postInitConfig();
        tile.init();
      }
    }
  }

  @Override
  public int getGridColumnCount() {
    return propertySupport.getPropertyInt(PROP_GRID_COLUMN_COUNT);
  }

  @Override
  public void setGridColumnCount(int gridColumnCount) {
    propertySupport.setPropertyInt(PROP_GRID_COLUMN_COUNT, gridColumnCount);
  }

  @Override
  public boolean isWithPlaceholders() {
    return propertySupport.getPropertyBool(PROP_WITH_PLACEHOLDERS);
  }

  @Override
  public void setWithPlaceholders(boolean withPlaceholders) {
    propertySupport.setPropertyBool(PROP_WITH_PLACEHOLDERS, withPlaceholders);
  }

  @Override
  public boolean isScrollable() {
    return propertySupport.getPropertyBool(PROP_SCROLLABLE);
  }

  @Override
  public void setScrollable(boolean scrollable) {
    propertySupport.setPropertyBool(PROP_SCROLLABLE, scrollable);
  }

  @Override
  public String getLogicalGrid() {
    return propertySupport.getPropertyString(PROP_LOGICAL_GRID);
  }

  @Override
  public void setLogicalGrid(String logicalGrid) {
    propertySupport.setPropertyString(PROP_LOGICAL_GRID, logicalGrid);
  }

  @Override
  public int getLogicalGridColumnWidth() {
    return propertySupport.getPropertyInt(PROP_LOGICAL_GRID_COLUMN_WIDTH);
  }

  @Override
  public void setLogicalGridColumnWidth(int logicalGridColumnWidth) {
    propertySupport.setPropertyInt(PROP_LOGICAL_GRID_COLUMN_WIDTH, logicalGridColumnWidth);
  }

  @Override
  public int getLogicalGridRowHeight() {
    return propertySupport.getPropertyInt(PROP_LOGICAL_GRID_ROW_HEIGHT);
  }

  @Override
  public void setLogicalGridRowHeight(int logicalGridRowHeight) {
    propertySupport.setPropertyInt(PROP_LOGICAL_GRID_ROW_HEIGHT, logicalGridRowHeight);
  }

  @Override
  public int getLogicalGridHGap() {
    return propertySupport.getPropertyInt(PROP_LOGICAL_GRID_H_GAP);
  }

  @Override
  public void setLogicalGridHGap(int logicalGridGap) {
    propertySupport.setPropertyInt(PROP_LOGICAL_GRID_H_GAP, logicalGridGap);
  }

  @Override
  public int getLogicalGridVGap() {
    return propertySupport.getPropertyInt(PROP_LOGICAL_GRID_V_GAP);
  }

  @Override
  public void setLogicalGridVGap(int logicalGridGap) {
    propertySupport.setPropertyInt(PROP_LOGICAL_GRID_V_GAP, logicalGridGap);
  }

  @Override
  public int getMaxContentWidth() {
    return propertySupport.getPropertyInt(PROP_MAX_CONTENT_WIDTH);
  }

  @Override
  public void setMaxContentWidth(int maxContentWidth) {
    propertySupport.setPropertyInt(PROP_MAX_CONTENT_WIDTH, maxContentWidth);
  }

  /**
   * @returns the preferred width based on grid column count, column width and horizontal gap. Typically used to set the
   *          max content width.
   */
  protected int calculatePreferredWidth() {
    return getGridColumnCount() * getLogicalGridColumnWidth() + (getGridColumnCount() - 1) * getLogicalGridHGap();
  }

  @Override
  public <T extends ITile> T getTileByClass(Class<T> tileClass) {
    // TODO [15.4] bsh: Make this method more sophisticated (@Replace etc.)
    T candidate = null;
    for (ITile tile : getTiles()) {
      if (tile.getClass() == tileClass) {
        return tileClass.cast(tile);
      }
      if (candidate == null && tileClass.isInstance(tile)) {
        candidate = tileClass.cast(tile);
      }
    }
    return candidate;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getContainer() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getContainer().classId();
    }
    return simpleClassId;
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls tiles.
   */
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  protected String getAsyncLoadIdentifier() {
    return null;
  }

  protected String getWindowIdentifier() {
    return null;
  }

  @Override
  public JobInput createAsyncLoadJobInput(ITile tile) {
    return Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent().withProperty(PROP_RUN_CONTEXT_TILE, tile))
        .withName(PROP_ASYNC_LOAD_JOBNAME_PREFIX)
        .withExecutionHint(PROP_ASYNC_LOAD_IDENTIFIER_PREFIX + getAsyncLoadIdentifier())
        .withExecutionHint(PROP_WINDOW_IDENTIFIER_PREFIX + getWindowIdentifier());
  }

  @Override
  public void ensureTileDataLoaded() {
    BEANS.get(TileDataLoadManager.class).cancel(getAsyncLoadIdentifier(), getWindowIdentifier());

    for (ITile tile : getTiles()) {
      tile.ensureDataLoaded();
    }
  }

  @Override
  public void loadTileData() {
    BEANS.get(TileDataLoadManager.class).cancel(getAsyncLoadIdentifier(), getWindowIdentifier());

    for (ITile tile : getTiles()) {
      tile.loadData();
    }
  }
}
