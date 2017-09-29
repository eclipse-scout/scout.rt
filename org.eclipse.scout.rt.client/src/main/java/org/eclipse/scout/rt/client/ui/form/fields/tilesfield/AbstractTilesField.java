package org.eclipse.scout.rt.client.ui.form.fields.tilesfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.tile.AbstractTiles;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITiles;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 7.1
 */
@ClassId("ee6298ff-ef88-4abd-bae0-f5764f6344d8")
public abstract class AbstractTilesField<T extends ITiles> extends AbstractFormField implements ITilesField<T> {

  public AbstractTilesField() {
    this(true);
  }

  public AbstractTilesField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setTiles(createTiles());
  }

  @SuppressWarnings("unchecked")
  protected T createTiles() {
    List<ITiles> contributedFields = m_contributionHolder.getContributionsByClass(ITiles.class);
    ITiles result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return (T) result;
    }

    Class<? extends ITiles> configuredTiles = getConfiguredTiles();
    if (configuredTiles != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredTiles);
    }
    return null;
  }

  private Class<? extends ITiles> getConfiguredTiles() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ITiles.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getTiles() {
    return (T) propertySupport.getProperty(PROP_TILES);
  }

  @Override
  public void setTiles(T tiles) {
    T oldTiles = getTiles();
    if (oldTiles == tiles) {
      return;
    }

    if (oldTiles instanceof AbstractTiles) {
      ((AbstractTiles) oldTiles).setContainerInternal(null);
    }
    propertySupport.setProperty(PROP_TILES, tiles);
    if (tiles instanceof AbstractTiles) {
      ((AbstractTiles) tiles).setContainerInternal(this);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a tiles field is 3.
   */
  @Override
  protected int getConfiguredGridH() {
    return 3;
  }

  @Override
  protected boolean execIsEmpty() {
    return getTiles().getTiles().isEmpty();
  }

  @Override
  public <K extends ITile> K getTileByClass(Class<K> tileClass) {
    return getTiles().getTileByClass(tileClass);
  }
}
