package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * @since 7.1
 */
@ClassId("c04e6cf7-fda0-4146-afea-6a0ff0a50c4b")
public abstract class AbstractTiles extends AbstractWidget implements ITiles {

  public AbstractTiles() {
    this(true);
  }

  public AbstractTiles(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setScrollable(getConfiguredScrollable());
    setLogicalGridRowHeight(getConfiguredLogicalGridRowHeight());
    setLogicalGridHGap(getConfiguredLogicalGridHGap());
    setLogicalGridVGap(getConfiguredLogicalGridVGap());
    setWithPlaceholders(getConfiguredWithPlaceholders());
    setGridColumnCount(getConfiguredGridColumnCount());

    OrderedCollection<ITile> tiles = new OrderedCollection<>();
    injectTilesInternal(tiles);
    setTiles(tiles.getOrderedList());
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
  @Order(10)
  protected int getConfiguredLogicalGridRowHeight() {
    return 150;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredLogicalGridHGap() {
    return 15;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredLogicalGridVGap() {
    return 20;
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
  public void setTiles(List<ITile> tiles) {
    propertySupport.setPropertyList(PROP_TILES, tiles);
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
  public int getLogicalGridRowHeight() {
    return propertySupport.getPropertyInt(PROP_LOGICAL_GRID_ROW_HEIGHT);
  }

  @Override
  public void setLogicalGridRowHeight(int logicalGridRowHeight) {
    propertySupport.setPropertyInt(PROP_LOGICAL_GRID_ROW_HEIGHT, logicalGridRowHeight);
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
}
