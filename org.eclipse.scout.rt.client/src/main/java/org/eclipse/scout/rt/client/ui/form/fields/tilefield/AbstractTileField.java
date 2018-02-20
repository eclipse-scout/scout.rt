package org.eclipse.scout.rt.client.ui.form.fields.tilefield;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 7.1
 */
@ClassId("ee6298ff-ef88-4abd-bae0-f5764f6344d8")
public abstract class AbstractTileField<T extends ITileGrid<? extends ITile>> extends AbstractFormField implements ITileField<T> {

  public AbstractTileField() {
    this(true);
  }

  public AbstractTileField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTileGrid(createTileGrid());
  }

  @SuppressWarnings("unchecked")
  protected T createTileGrid() {
    List<ITileGrid> contributedFields = m_contributionHolder.getContributionsByClass(ITileGrid.class);
    ITileGrid result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return (T) result;
    }

    Class<? extends ITileGrid> configuredTileGrid = getConfiguredTileGrid();
    if (configuredTileGrid != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredTileGrid);
    }
    return null;
  }

  private Class<? extends ITileGrid> getConfiguredTileGrid() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ITileGrid.class);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getTileGrid()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getTileGrid() {
    return (T) propertySupport.getProperty(PROP_TILE_GRID);
  }

  @Override
  public void setTileGrid(T tiles) {
    T oldTileGrid = getTileGrid();
    if (oldTileGrid == tiles) {
      return;
    }

    if (oldTileGrid instanceof AbstractTileGrid) {
      ((AbstractTileGrid) oldTileGrid).setContainerInternal(null);
    }
    propertySupport.setProperty(PROP_TILE_GRID, tiles);
    if (tiles instanceof AbstractTileGrid) {
      ((AbstractTileGrid) tiles).setContainerInternal(this);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a tile field is 3.
   */
  @Override
  protected int getConfiguredGridH() {
    return 3;
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  @Override
  protected boolean execIsEmpty() {
    if (!super.execIsEmpty()) {
      return false;
    }
    return getTileGrid().getTiles().isEmpty();
  }
}
