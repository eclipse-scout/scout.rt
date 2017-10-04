package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.tile.TileChains.TileDisposeTileChain;
import org.eclipse.scout.rt.client.ui.tile.TileChains.TileInitTileChain;
import org.eclipse.scout.rt.client.ui.tile.TileChains.TileLoadDataTileChain;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.shared.data.tile.ITileColorScheme;
import org.eclipse.scout.rt.shared.data.tile.TileColorScheme;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 7.1
 */
@ClassId("126ee77e-7e43-4b7b-94b4-a00f255e2492")
public abstract class AbstractTile extends AbstractWidget implements ITile {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTile.class);

  private final ObjectExtensions<AbstractTile, ITileExtension<? extends AbstractTile>> m_objectExtensions;
  private ITiles m_container;
  private DataChangeListener m_internalDataChangeListener;
  private boolean m_loaded = false;
  private boolean m_loadingLocked = false;

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
  protected void callInitializer() {
    interceptInitConfig();
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setOrder(calculateViewOrder());
    setColorScheme(getConfiguredColorScheme());
    setCssClass(getConfiguredCssClass());
    // FIXME CGU tiles maybe better create getConfiguredGridDataHints and enhance GridData with "with" pattern
    setGridDataHints(new GridData(getConfiguredGridX(), getConfiguredGridY(), getConfiguredGridW(), getConfiguredGridH(), getConfiguredGridWeightX(), getConfiguredGridWeightY(), false, false, -1, -1, true, true, 0, 0));
  }

  @Override
  public void postInitConfig() {
    // NOP
  }

  @Override
  public final void init() {
    try {
      initInternal();
      interceptInitTile();
    }
    catch (Exception e) {
      handleInitException(e);
    }
  }

  protected void initInternal() {
    if (getContainer() == null) {
      throw new IllegalStateException("Tile is not connected to a container");
    }
    if (getConfiguredAutoLoadDataOnInit()) {
      loadData();
    }
  }

  protected void handleInitException(Exception exception) {
    throw new PlatformException("Exception occured while initializing tile", exception);
  }

  protected void execInitTile() {
    // NOP
  }

  @Override
  public final void dispose() {
    disposeInternal();
    interceptDisposeTile();
  }

  protected void disposeInternal() {
    // NOP
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
          Order order = (Order) cls.getAnnotation(Order.class);
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
  protected ITileColorScheme getConfiguredColorScheme() {
    return TileColorScheme.DEFAULT;
  }

  /**
   * Configures the css class(es) of this tile.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(55)
  protected String getConfiguredCssClass() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredGridW() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(30)
  protected int getConfiguredGridH() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(40)
  protected int getConfiguredGridX() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(50)
  protected int getConfiguredGridY() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(60)
  protected int getConfiguredGridWeightX() {
    return -1;
  }

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
  public ITileColorScheme getColorScheme() {
    return (ITileColorScheme) propertySupport.getProperty(PROP_COLOR_SCHEME);
  }

  @Override
  public void setColorScheme(ITileColorScheme colorScheme) {
    propertySupport.setProperty(PROP_COLOR_SCHEME, colorScheme);
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }

  protected DataChangeListener getInternalDataChangeListener() {
    return m_internalDataChangeListener;
  }

  protected void setInternalDataChangeListener(DataChangeListener internalDataChangeListener) {
    m_internalDataChangeListener = internalDataChangeListener;
  }

  @Override
  public boolean isLoading() {
    return propertySupport.getPropertyBool(PROP_LOADING);
  }

  @Override
  public void setLoading(boolean loading) {
    propertySupport.setPropertyBool(PROP_LOADING, loading);
  }

  protected boolean isLoaded() {
    return m_loaded;
  }

  protected void setLoaded(boolean loaded) {
    m_loaded = loaded;
  }

  /**
   * Register a {@link DataChangeListener} on the desktop for these dataTypes<br>
   * Example:
   *
   * <pre>
   * registerDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = (WeakDataChangeListener) innerDataTypes -> {
        if (isLoaded()) {
          execDataChanged(innerDataTypes);
        }
      };
    }
    IDesktop.CURRENT.get().addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these dataTypes<br>
   * Example:
   *
   * <pre>
   * unregisterDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      ClientSessionProvider.currentSession().getDesktop().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  /**
   * see {@link IDesktop#dataChanged(Object...)}
   */
  protected void execDataChanged(Object... dataTypes) {
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
  public ITiles getContainer() {
    return m_container;
  }

  @Override
  public void setContainer(ITiles container) {
    m_container = container;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    return getContainer().classId() + ID_CONCAT_SYMBOL + simpleClassId;
  }

  @Override
  public void ensureDataLoaded() {
    if (!isLoaded()) {
      loadData();
    }
  }

  @Override
  public void loadData() {
    synchronized (this) {
      if (m_loadingLocked) {
        return;
      }
      m_loadingLocked = true;
    }
    beforeLoadData();
    try {
      interceptLoadData();
    }
    catch (Exception e) {
      handleLoadDataException(e);
    }
    finally {
      m_loadingLocked = false;
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
    return getClass().getSimpleName() + " [m_container=" + m_container + "]";
  }

  /**
   * Uses the exception to set a corresponding error status on the tile field.
   */
  protected void handleLoadDataException(Exception e) {
    if (e instanceof VetoException) {
      LOG.info("VetoException on {}: {}", this.getClass().getName(), e.getMessage());
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

    @Override
    public void loadData() {
      setLoading(true);
      try {
        BEANS.get(TileDataLoadManager.class).schedule(() -> doLoadData(), m_container.createAsyncLoadJobInput(AbstractTile.this));
      }
      catch (RuntimeException e) {
        setLoading(false);
        handleLoadDataException(e);
      }
    }

    private void doLoadData() {
      try {
        final DATA data = loadDataAsync();

        // currently not in model jobs, thus synchronize first to set field data
        ModelJobs.schedule(() -> updateModelData(data), ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("setting tile data"));
        setLoaded(true);
      }
      catch (final Exception e) {
        if (RunMonitor.CURRENT.get().isCancelled()) {
          setLoaded(false);
        }
        else {
          // if a general load error occurred loading will probably continue to fail, do not keep retrying
          setLoaded(true);
        }
        // currently not in model jobs, thus synchronize first to set error status
        ModelJobs.schedule(() -> {
          setLoading(false);
          handleLoadDataException(e);
        }, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("handling exception while loading tile data"));
      }
    }

    private void updateModelData(final DATA data) {
      setLoading(false);
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

  }
}
