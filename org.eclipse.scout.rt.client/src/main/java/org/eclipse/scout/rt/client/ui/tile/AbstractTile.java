/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.tile.ITileExtension;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileDataChangedTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileDisposeTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileInitTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileLoadDataTileChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.data.colorscheme.ColorScheme;
import org.eclipse.scout.rt.shared.data.colorscheme.IColorScheme;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 8.0
 */
@ClassId("126ee77e-7e43-4b7b-94b4-a00f255e2492")
public abstract class AbstractTile extends AbstractWidget implements ITile {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTile.class);

  private final ObjectExtensions<AbstractTile, ITileExtension<? extends AbstractTile>> m_objectExtensions;
  private IDataChangeListener m_internalDataChangeListener;
  private boolean m_filterAccepted = true;
  private volatile boolean m_loaded = false;

  public AbstractTile() {
    this(true);
  }

  public AbstractTile(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setOrder(calculateViewOrder());
    setColorScheme(getConfiguredColorScheme());
    setCssClass(getConfiguredCssClass());
    setDisplayStyle(getConfiguredDisplayStyle());
    setGridDataHints(getConfiguredGridDataHints());
  }

  @Override
  protected final void initInternal() {
    super.initInternal();
    try {
      initTileInternal();
      interceptInitTile();
    }
    catch (Exception e) {
      handleInitException(e);
    }
  }

  protected void initTileInternal() {
    Assertions.assertNotNull(getParent(), "Tile is not connected to a container");
    if (getConfiguredAutoLoadDataOnInit()) {
      loadData();
    }
  }

  protected void handleInitException(Exception exception) {
    throw new PlatformException("Exception occurred while initializing tile", exception);
  }

  protected void execInitTile() {
    // NOP
  }

  @Override
  protected final void disposeInternal() {
    disposeTileInternal();
    interceptDisposeTile();
    super.disposeInternal();
  }

  protected void disposeTileInternal() {
    unregisterDataChangeListener();
  }

  protected void execDisposeTile() {
    // NOP
  }

  /**
   * Calculates the tiles's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If no
   * {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  @SuppressWarnings("squid:S1244")
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && ITile.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  /**
   * Configures the view order of this tile. The view order determines the order in which the tiles appear in the tile
   * box. The order of tiles with no view order configured ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the tile class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this tile.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected IColorScheme getConfiguredColorScheme() {
    return ColorScheme.DEFAULT;
  }

  /**
   * Configures the display style of the tile.
   * <p>
   * The available styles are:
   * <ul>
   * <li>{@link IFormFieldTile#DISPLAY_STYLE_PLAIN}</li>
   * <li>{@link IFormFieldTile#DISPLAY_STYLE_DASHBOARD}</li>
   * </ul>
   * <p>
   * Subclasses can override this method. The default is {@link IFormFieldTile#DISPLAY_STYLE_DASHBOARD}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * Configures the grid data for this tile.
   * <p>
   * The typical approach to configure it is to get the default object by calling
   * <code>super.getConfiguredGridDataHints()</code> and the using the "with" methods to adjust the properties.
   * <p>
   * <b>Example:</b><br>
   *
   * <pre>
   * &#64;Override
   * protected GridData getConfiguredGridDataHints() {
   *   return super.getConfiguredGridDataHints()
   *       .withW(2)
   *       .withUseUiHeight(true);
   * }
   * </pre>
   *
   * The most common methods are also available as separate getConfigured methods (e.g. {@link #getConfiguredGridW()},
   * {@link #getConfiguredGridH()}).
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(15)
  protected GridData getConfiguredGridDataHints() {
    return new GridData(-1, -1, getConfiguredGridW(), getConfiguredGridH(),
        getConfiguredGridWeightX(), getConfiguredGridWeightY(), false, false, -1, -1, true, true, 0, 0);
  }

  /**
   * @see #getConfiguredGridDataHints()
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredGridW() {
    return 1;
  }

  /**
   * @see #getConfiguredGridDataHints()
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(30)
  protected int getConfiguredGridH() {
    return 1;
  }

  /**
   * @see #getConfiguredGridDataHints()
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(60)
  protected int getConfiguredGridWeightX() {
    return -1;
  }

  /**
   * @see #getConfiguredGridDataHints()
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected int getConfiguredGridWeightY() {
    return 0;
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public GridData getGridDataHints() {
    return new GridData((GridData) propertySupport.getProperty(PROP_GRID_DATA_HINTS));
  }

  @Override
  public void setGridDataHints(GridData hints) {
    propertySupport.setProperty(PROP_GRID_DATA_HINTS, new GridData(hints));
  }

  @Override
  public IColorScheme getColorScheme() {
    return (IColorScheme) propertySupport.getProperty(PROP_COLOR_SCHEME);
  }

  @Override
  public void setColorScheme(IColorScheme colorScheme) {
    propertySupport.setProperty(PROP_COLOR_SCHEME, colorScheme);
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  /**
   * Calling this method after initialization won't have any effect
   */
  protected void setDisplayStyle(String style) {
    propertySupport.setPropertyString(PROP_DISPLAY_STYLE, style);
  }

  protected IDataChangeListener getInternalDataChangeListener() {
    return m_internalDataChangeListener;
  }

  protected void setInternalDataChangeListener(IDataChangeListener internalDataChangeListener) {
    m_internalDataChangeListener = internalDataChangeListener;
  }

  public boolean isLoaded() {
    return m_loaded;
  }

  public void setLoaded(boolean loaded) {
    m_loaded = loaded;
  }

  @Override
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = event -> {
        if (isLoaded()) {
          interceptDataChanged(event);
        }
      };
    }
    IDesktop.CURRENT.get().dataChangeListeners().add(m_internalDataChangeListener, true, dataTypes);
  }

  @Override
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      ClientSessionProvider.currentSession().getDesktop().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  /**
   * see {@link IDesktop#dataChanged(Object...)} and
   * {@link IDesktop#fireDataChangeEvent(org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent)}
   */
  protected void execDataChanged(DataChangeEvent event) {
    loadData();
  }

  /**
   * Configures whether to automatically call {@link #loadData()}} on initialization of the the tile. The default is
   * <code>true</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredAutoLoadDataOnInit() {
    return true;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    IWidget parent = getParent();
    if (parent == null) {
      return simpleClassId;
    }
    return parent.classId() + ID_CONCAT_SYMBOL + simpleClassId;
  }

  @Override
  public void setFilterAccepted(boolean filterAccepted) {
    m_filterAccepted = filterAccepted;
  }

  @Override
  public boolean isFilterAccepted() {
    return m_filterAccepted;
  }

  @Override
  public void ensureDataLoaded() {
    if (!isLoaded()) {
      loadData();
    }
  }

  @Override
  public void loadData() {
    if (isLoading()) {
      return;
    }
    beforeLoadData();
    try {
      interceptLoadData();
    }
    catch (Exception e) {
      handleLoadDataException(e);
    }
  }

  protected void beforeLoadData() {
    // NOP
  }

  /**
   * The default implementation loads the data via a {@link ITileDataLoader} if provided.
   */
  protected void execLoadData() {
    ITileDataLoader dataLoader = createDataLoader();
    if (dataLoader != null) {
      dataLoader.loadData();
    }
  }

  /**
   * Creates a tile data loader.
   */
  protected ITileDataLoader createDataLoader() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [m_container=" + getParent() + "]";
  }

  /**
   * Uses the exception to set a corresponding error status on the tile field.
   */
  protected void handleLoadDataException(Throwable e) {
    if (e instanceof VetoException) {
      LOG.info("VetoException on {}: {}", this.getClass().getName(), e.getMessage());
    }
    else if (e instanceof FutureCancelledError) {
      LOG.debug("FutureCancelledError on {}: {}", this.getClass().getName(), e.getMessage());
    }
    else if (e instanceof ThreadInterruptedError) {
      LOG.debug("ThreadInterruptedError on {}: {}", this.getClass().getName(), e.getMessage());
    }
    else {
      LOG.error("Unexpected error on {}", this.getClass().getName(), e);
    }
  }

  @FunctionalInterface
  public interface ITileDataLoader {

    void loadData();
  }

  /**
   * Abstract implementation of a tile data loader supporting async loading and proper error handling.
   */
  public abstract class AbstractTileDataLoader<DATA> implements ITileDataLoader {

    /**
     * Method to load the data. Executed outside a model job (async).
     *
     * @return Data to load
     */
    protected abstract DATA loadDataAsync();

    /**
     * Method to set the data for the tile field. Executed within a model job.
     *
     * @param data
     *          Data loaded by {@link #loadDataAsync()}
     */
    protected abstract void setTileData(DATA data);

    @SuppressWarnings("squid:S1181")
    @Override
    public void loadData() {
      if (isLoading()) {
        return;
      }
      setLoading(true);
      try {
        ITileGrid tileGridParent = getParentOfType(ITileGrid.class);
        IForm formParent = getParentOfType(IForm.class);
        BEANS.get(TileDataLoadManager.class).schedule(() -> {
          try {
            final DATA data = doLoadData();
            BEANS.get(TileDataLoadManager.class).runInModelJob(() -> {
              setLoading(false);
              updateModelData(data);
            });
          }
          catch (final Throwable e) { // Catch Throwable so we can handle all AbstractInterruptionError accordingly
            BEANS.get(TileDataLoadManager.class).runInModelJob(() -> {
              setLoading(false);
              handleLoadDataException(e);
            });
          }
        }, tileGridParent != null
            ? tileGridParent.createAsyncLoadJobInput(AbstractTile.this)
            : ModelJobs.newInput(ClientRunContexts.copyCurrent()
                .withForm(formParent != null ? formParent : IForm.CURRENT.get())));
      }
      catch (RuntimeException e) {
        setLoading(false);
        handleLoadDataException(e);
      }
    }

    protected DATA doLoadData() {
      try {
        DATA data = loadDataAsync();
        setLoaded(true);
        return data;
      }
      catch (final Exception e) {
        // if a general load error occurred loading will prob continue fail, do not keep retrying
        setLoaded(!RunMonitor.CURRENT.get().isCancelled());
        throw e;
      }
    }

    protected void updateModelData(final DATA data) {
      try {
        setTileData(data);
      }
      catch (Exception e) {
        handleLoadDataException(e);
      }
    }
  }

  @Override
  public void onLoadDataCancel() {
    setLoading(false);
  }

  @Override
  public final List<? extends ITileExtension<? extends AbstractTile>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected final void interceptDisposeTile() {
    List<? extends ITileExtension<? extends AbstractTile>> extensions = getAllExtensions();
    TileDisposeTileChain chain = new TileDisposeTileChain(extensions);
    chain.execDisposeTile();
  }

  protected final void interceptInitTile() {
    List<? extends ITileExtension<? extends AbstractTile>> extensions = getAllExtensions();
    TileInitTileChain chain = new TileInitTileChain(extensions);
    chain.execInitTile();
  }

  protected final void interceptLoadData() {
    List<? extends ITileExtension<? extends AbstractTile>> extensions = getAllExtensions();
    TileLoadDataTileChain chain = new TileLoadDataTileChain(extensions);
    chain.execLoadData();
  }

  protected final void interceptDataChanged(DataChangeEvent event) {
    List<? extends ITileExtension<? extends AbstractTile>> extensions = getAllExtensions();
    TileDataChangedTileChain chain = new TileDataChangedTileChain(extensions);
    chain.execDataChanged(event);
  }

  protected ITileExtension<? extends AbstractTile> createLocalExtension() {
    return new LocalTileExtension<>(this);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalTileExtension<OWNER extends AbstractTile> extends AbstractExtension<OWNER> implements ITileExtension<OWNER> {

    public LocalTileExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDisposeTile(TileDisposeTileChain chain) {
      getOwner().execDisposeTile();
    }

    @Override
    public void execInitTile(TileInitTileChain chain) {
      getOwner().execInitTile();
    }

    @Override
    public void execLoadData(TileLoadDataTileChain chain) {
      getOwner().execLoadData();
    }

    @Override
    public void execDataChanged(TileDataChangedTileChain chain, DataChangeEvent event) {
      getOwner().execDataChanged(event);
    }
  }
}
