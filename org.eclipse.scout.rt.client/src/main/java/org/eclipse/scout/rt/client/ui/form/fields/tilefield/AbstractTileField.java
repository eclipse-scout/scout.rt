/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tilefield;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.ITileFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 8.0
 */
@ClassId("ee6298ff-ef88-4abd-bae0-f5764f6344d8")
public abstract class AbstractTileField<T extends ITileGrid<? extends ITile>> extends AbstractFormField implements ITileField<T> {
  private ITileFieldUIFacade<T> m_uiFacade;

  public AbstractTileField() {
    this(true);
  }

  public AbstractTileField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

    super.initConfig();
    setTileGrid(createTileGrid());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setDropType(getConfiguredDropType());
    setDragType(getConfiguredDragType());
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

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(10)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * Configures the drop support of this tile field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(20)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the drag support of this tile field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(30)
  protected int getConfiguredDragType() {
    return 0;
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

    if (oldTileGrid != null) {
      oldTileGrid.setParentInternal(null);
    }
    propertySupport.setProperty(PROP_TILE_GRID, tiles);
    if (tiles != null) {
      tiles.setParentInternal(this);
    }
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(IDNDSupport.PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(IDNDSupport.PROP_DROP_TYPE);
  }

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(IDNDSupport.PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(IDNDSupport.PROP_DROP_MAXIMUM_SIZE);
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

  @ConfigOperation
  @Order(10)
  protected void execDropRequest(TransferObject transferObject) {
  }

  @ConfigOperation
  @Order(20)
  protected TransferObject execDragRequest() {
    return null;
  }

  /*
   * UI accessible
   */
  @Override
  public ITileFieldUIFacade<T> getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements ITileFieldUIFacade<T> {

    @Override
    public TransferObject fireDragRequestFromUI() {
      TransferObject t = null;
      t = interceptDragRequest();
      return t;
    }

    @Override
    public void fireDropActionFromUI(TransferObject scoutTransferable) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        //can not drop anything into field if its disabled.
        return;
      }
      interceptDropRequest(scoutTransferable);
    }
  }

  @Override
  protected ITileFieldExtension<T, ? extends AbstractTileField> createLocalExtension() {
    return new LocalTileFieldExtension<>(this);
  }

  protected static class LocalTileFieldExtension<T extends ITileGrid<? extends ITile>, OWNER extends AbstractTileField<T>> extends LocalFormFieldExtension<OWNER> implements ITileFieldExtension<T, OWNER> {

    public LocalTileFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public TransferObject execDragRequest(TileFieldDragRequestChain chain) {
      return getOwner().execDragRequest();
    }

    @Override
    public void execDropRequest(TileFieldDropRequestChain chain, TransferObject transferObject) {
      getOwner().execDropRequest(transferObject);
    }
  }

  protected final TransferObject interceptDragRequest() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TileFieldDragRequestChain<T> chain = new TileFieldDragRequestChain<>(extensions);
    return chain.execDragRequest();
  }

  protected final void interceptDropRequest(TransferObject transferObject) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TileFieldDropRequestChain<T> chain = new TileFieldDropRequestChain<>(extensions);
    chain.execDropRequest(transferObject);
  }
}
