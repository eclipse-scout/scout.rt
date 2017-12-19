package org.eclipse.scout.rt.client.ui.tile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.tile.ITileGridExtension;
import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITilesContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TilesContextMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

/**
 * @since 7.1
 */
@ClassId("c04e6cf7-fda0-4146-afea-6a0ff0a50c4b")
public abstract class AbstractTileGrid<T extends ITile> extends AbstractWidget implements ITileGrid<T> {
  private ITileGridUIFacade m_uiFacade;
  private final ObjectExtensions<AbstractTileGrid, ITileGridExtension<T, ? extends AbstractTileGrid>> m_objectExtensions;
  private ContributionComposite m_contributionHolder;
  private List<ITileFilter> m_filters;
  private boolean m_filteredRowsDirty = false;
  private Comparator<T> m_comparator;

  public AbstractTileGrid() {
    this(true);
  }

  public AbstractTileGrid(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    m_filters = new ArrayList<>(1);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfigInternal() {
    interceptInitConfig();
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setGridColumnCount(getConfiguredGridColumnCount());
    setLogicalGrid(getConfiguredLogicalGrid());
    setLayoutConfig(getConfiguredLayoutConfig());
    setMultiSelect(getConfiguredMultiSelect());
    setSelectable(getConfiguredSelectable());
    setScrollable(getConfiguredScrollable());
    setWithPlaceholders(getConfiguredWithPlaceholders());

    OrderedCollection<T> tiles = new OrderedCollection<>();
    injectTilesInternal(tiles);
    setSelectedTiles(new ArrayList<>());
    setFilteredTiles(new ArrayList<>());
    setTiles(tiles.getOrderedList());
    initMenus();

    // local property observer
    addPropertyChangeListener(new P_PropertyChangeListener());
  }

  protected void initMenus() {
    List<Class<? extends IMenu>> ma = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> clazz : ma) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, clazz);
      menus.addOrdered(menu);
    }
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);

    // set container on menus
    for (IMenu menu : menus) {
      menu.setContainerInternal(this);
    }

    new MoveActionNodesHandler<>(menus).moveModelObjects();
    ITilesContextMenu contextMenu = new TilesContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);
  }

  protected void injectTilesInternal(OrderedCollection<T> tiles) {
    List<Class<? extends ITile>> tileClasses = getConfiguredTiles();
    for (Class<? extends ITile> tileClass : tileClasses) {
      T tile = createTileInternal(tileClass);
      if (tile != null) {
        tiles.addOrdered(tile);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected T createTileInternal(Class<? extends ITile> tileClass) {
    return (T) ConfigurationUtility.newInnerInstance(this, tileClass);
  }

  protected List<Class<? extends ITile>> getConfiguredTiles() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITile>> filtered = ConfigurationUtility.filterClasses(dca, ITile.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public void setMenus(List<? extends IMenu> menus) {
    getContextMenu().setChildActions(menus);
  }

  protected void setContextMenu(ITilesContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public ITilesContextMenu getContextMenu() {
    return (ITilesContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <M extends IMenu> M getMenuByClass(Class<M> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  protected ITileGridUIFacade createUIFacade() {
    return new P_TileGridUIFacade();
  }

  @Override
  public ITileGridUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected void initInternal() {
    super.initInternal();
    for (T tile : getTilesInternal()) {
      tile.init();
    }
  }

  @Override
  protected void postInitConfigInternal() {
    super.postInitConfigInternal();
    for (T tile : getTilesInternal()) {
      tile.postInitConfig();
    }
  }

  @Override
  protected void disposeInternal() {
    for (T tile : getTilesInternal()) {
      tile.dispose();
    }
    super.disposeInternal();
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

  /**
   * Configures the layout hints.
   * <p>
   * You may use {@link #calculatePreferredWidth()} if you want to limit the max width based on the column count, column
   * width and column gap.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(10)
  protected TileGridLayoutConfig getConfiguredLayoutConfig() {
    return new TileGridLayoutConfig();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredWithPlaceholders() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredSelectable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredMultiSelect() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredScrollable() {
    return true;
  }

  @Override
  public void setComparator(Comparator<T> comparator) {
    setComparator(comparator, true);
  }

  @Override
  public void setComparator(Comparator<T> comparator, boolean sortNow) {
    if (m_comparator == comparator) {
      return;
    }
    m_comparator = comparator;
    if (sortNow) {
      sort();
    }
  }

  @Override
  public Comparator<T> getComparator() {
    return m_comparator;
  }

  @Override
  public void sort() {
    if (m_comparator == null) {
      return;
    }
    List<T> tiles = getTiles();
    sortInternal(tiles);
    setTilesInternal(tiles);
  }

  public void sortInternal(List<T> tiles) {
    if (m_comparator == null) {
      return;
    }
    tiles.sort(m_comparator);
  }

  @Override
  public List<T> getTiles() {
    return CollectionUtility.arrayList(propertySupport.getPropertyList(PROP_TILES));
  }

  @Override
  public int getTileCount() {
    return getTilesInternal().size();
  }

  /**
   * @return the live list of the tiles
   */
  public List<T> getTilesInternal() {
    return propertySupport.getPropertyList(PROP_TILES);
  }

  @Override
  public void setTiles(List<T> tiles) {
    if (CollectionUtility.equalsCollection(getTilesInternal(), tiles, true)) {
      return;
    }
    List<T> existingTiles = ObjectUtility.nvl(getTilesInternal(), new ArrayList<>());
    tiles = ObjectUtility.nvl(tiles, new ArrayList<>());

    // Dispose old tiles (only if they are not in the new list)
    List<T> tilesToDelete = new ArrayList<>(existingTiles);
    tilesToDelete.removeAll(tiles);
    deleteTilesInternal(tilesToDelete);
    deselectTiles(tilesToDelete);

    // Initialize new tiles
    // Only initialize when tiles are added later,
    // if they are added while initConfig runs, initTiles() will take care of the initialization which will be called by the container (e.g. TileField)
    List<T> tilesToInsert = new ArrayList<>(tiles);
    tilesToInsert.removeAll(existingTiles);
    addTilesInternal(tilesToInsert);

    sortInternal(tiles);
    setTilesInternal(tiles);

    m_filteredRowsDirty = true;
    applyFilters(tilesToInsert);
  }

  protected void deleteTilesInternal(List<T> tilesToDelete) {
    for (T tile : tilesToDelete) {
      deleteTileInternal(tile);
    }
  }

  protected void deleteTileInternal(T tile) {
    tile.dispose();
  }

  protected void addTilesInternal(List<T> tilesToInsert) {
    for (T tile : tilesToInsert) {
      addTileInternal(tile);
    }
    // Initialize after every tile has been linked to the container, so that it is possible to access other tiles in tile.execInit
    if (isInitConfigDone()) {
      for (T tile : tilesToInsert) {
        tile.postInitConfig();
        tile.init();
      }
    }
  }

  protected void addTileInternal(T tile) {
    tile.setContainer(this);
  }

  protected void setTilesInternal(List<T> tiles) {
    propertySupport.setPropertyList(PROP_TILES, tiles);
  }

  @Override
  public void addTiles(List<T> tilesToAdd) {
    List<T> tiles = new ArrayList<>(getTilesInternal());
    tiles.addAll(tilesToAdd);
    setTiles(tiles);
  }

  @Override
  public void addTile(T tile) {
    addTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void deleteTiles(List<T> tilesToDelete) {
    List<T> tiles = new ArrayList<>(getTilesInternal());
    tiles.removeAll(tilesToDelete);
    setTiles(tiles);
  }

  @Override
  public void deleteTile(T tile) {
    deleteTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void deleteAllTiles() {
    setTiles(new ArrayList<>());
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
  public boolean isSelectable() {
    return propertySupport.getPropertyBool(PROP_SELECTABLE);
  }

  @Override
  public void setSelectable(boolean selectable) {
    propertySupport.setPropertyBool(PROP_SELECTABLE, selectable);
  }

  @Override
  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  @Override
  public void setMultiSelect(boolean multiSelect) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, multiSelect);
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
  public TileGridLayoutConfig getLayoutConfig() {
    return (TileGridLayoutConfig) propertySupport.getProperty(PROP_LAYOUT_CONFIG);
  }

  @Override
  public void setLayoutConfig(TileGridLayoutConfig layoutConfig) {
    propertySupport.setProperty(PROP_LAYOUT_CONFIG, layoutConfig);
  }

  /**
   * Called whenever the selection changes.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param tiles
   *          an unmodifiable list of the selected tiles, may be empty but not null.
   */
  @ConfigOperation
  @Order(100)
  protected void execTilesSelected(List<T> tiles) {
  }

  @Override
  public void selectTiles(List<T> tiles) {
    if (!isSelectable()) {
      setSelectedTiles(new ArrayList<>());
      return;
    }

    List<T> newSelection = filterTiles(tiles);
    if (newSelection.size() > 1 && !isMultiSelect()) {
      T first = newSelection.get(0);
      newSelection.clear();
      newSelection.add(first);
    }
    if (!CollectionUtility.equalsCollection(getSelectedTilesInternal(), newSelection, false)) {
      setSelectedTiles(newSelection);
    }
  }

  @Override
  public void selectTile(T tile) {
    selectTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void selectAllTiles() {
    selectTiles(getTilesInternal());
  }

  @Override
  public void deselectTiles(List<T> tiles) {
    List<T> selectedTiles = getSelectedTiles();
    boolean selectionChanged = selectedTiles.removeAll(tiles);
    if (selectionChanged) {
      selectTiles(selectedTiles);
    }
  }

  @Override
  public void deselectTile(T tile) {
    deselectTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void deselectAllTiles() {
    selectTiles(new ArrayList<>());
  }

  @Override
  public List<T> getSelectedTiles() {
    return CollectionUtility.arrayList(propertySupport.getPropertyList(PROP_SELECTED_TILES));
  }

  /**
   * @return the live list of the selected tiles
   */
  public List<T> getSelectedTilesInternal() {
    return propertySupport.getPropertyList(PROP_SELECTED_TILES);
  }

  @Override
  public int getSelectedTileCount() {
    return getSelectedTilesInternal().size();
  }

  protected void setSelectedTiles(List<T> tiles) {
    propertySupport.setPropertyList(PROP_SELECTED_TILES, tiles);
  }

  @Override
  public T getSelectedTile() {
    if (getSelectedTileCount() == 0) {
      return null;
    }
    return getSelectedTiles().get(0);
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
  public T getTileByClass(Class<T> tileClass) {
    // TODO [15.4] bsh: Make this method more sophisticated (@Replace etc.)
    T candidate = null;
    for (T tile : getTilesInternal()) {
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

    for (T tile : getTilesInternal()) {
      tile.ensureDataLoaded();
    }
  }

  @Override
  public void loadTileData() {
    BEANS.get(TileDataLoadManager.class).cancel(getAsyncLoadIdentifier(), getWindowIdentifier());

    for (T tile : getTilesInternal()) {
      tile.loadData();
    }
  }

  @Override
  public List<ITileFilter> getFilters() {
    return CollectionUtility.arrayList(m_filters);
  }

  @Override
  public void addFilter(ITileFilter filter, boolean applyFilters) {
    if (filter == null || m_filters.contains(filter)) {
      return;
    }
    m_filters.add(filter);
    if (applyFilters) {
      filter();
    }
  }

  @Override
  public void addFilter(ITileFilter filter) {
    addFilter(filter, true);
  }

  @Override
  public void removeFilter(ITileFilter filter, boolean applyFilters) {
    if (filter != null && m_filters.remove(filter) && applyFilters) {
      filter();
    }
  }

  @Override
  public void removeFilter(ITileFilter filter) {
    removeFilter(filter, true);
  }

  @Override
  public void filter() {
    // Full reset is set to true to loop through every tile and make sure tile.filterAccepted is correctly set
    applyFilters(true);
  }

  protected boolean applyFilters() {
    return applyFilters(getTilesInternal(), false);
  }

  protected boolean applyFilters(boolean fullReset) {
    return applyFilters(getTilesInternal(), fullReset);
  }

  protected boolean applyFilters(List<T> tiles) {
    return applyFilters(tiles, false);
  }

  protected boolean applyFilters(List<T> tiles, boolean fullReset) {
    if (m_filters.size() == 0 && !fullReset) {
      setFilteredTiles(getTilesInternal());
      m_filteredRowsDirty = false;
      return false;
    }
    boolean filterChanged = false;
    List<T> newlyHiddenTiles = new ArrayList<>();
    for (T tile : tiles) {
      boolean wasFilterAccepted = tile.isFilterAccepted();
      applyFilters(tile);
      if (tile.isFilterAccepted() != wasFilterAccepted) {
        filterChanged = true;
      }
      if (filterChanged && !tile.isFilterAccepted()) {
        newlyHiddenTiles.add(tile);
      }
    }

    // Non visible tiles must be deselected
    deselectTiles(newlyHiddenTiles);

    if (filterChanged || m_filteredRowsDirty) {
      setFilteredTiles(filterTiles(getTilesInternal()));
      m_filteredRowsDirty = false;
    }

    return filterChanged;
  }

  protected void applyFilters(ITile tile) {
    tile.setFilterAccepted(true);
    for (ITileFilter filter : m_filters) {
      if (!filter.accept(tile)) {
        tile.setFilterAccepted(false);
      }
    }
  }

  @Override
  public List<T> getFilteredTiles() {
    return propertySupport.getPropertyList(PROP_FILTERED_TILES);
  }

  protected void setFilteredTiles(List<T> tiles) {
    propertySupport.setPropertyList(PROP_FILTERED_TILES, tiles);
  }

  @Override
  public int getFilteredTileCount() {
    return getFilteredTiles().size();
  }

  protected List<T> filterTiles(List<T> tiles) {
    if (m_filters.isEmpty()) {
      return new ArrayList<>(tiles);
    }
    return tiles
        .stream()
        .filter((t) -> t.isFilterAccepted())
        .collect(Collectors.toList());
  }

  protected class P_TileGridUIFacade implements ITileGridUIFacade<T> {

    @Override
    public void setSelectedTilesFromUI(List<T> tiles) {
      selectTiles(tiles);
    }

  }

  @Override
  public final <C> C optContribution(Class<C> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <C> List<C> getContributionsByClass(Class<C> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <C> C getContribution(Class<C> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  @Override
  public final List<ITileGridExtension<T, ? extends AbstractTileGrid>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <E extends IExtension<?>> E getExtension(Class<E> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected ITileGridExtension<T, ? extends AbstractTileGrid> createLocalExtension() {
    return new LocalTilesExtension<>(this);
  }

  protected static class LocalTilesExtension<T extends ITile, TILES extends AbstractTileGrid<T>> extends AbstractExtension<TILES> implements ITileGridExtension<T, TILES> {

    public LocalTilesExtension(TILES owner) {
      super(owner);
    }

    @Override
    public void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles) {
      getOwner().execTilesSelected(tiles);
    }
  }

  protected final void interceptTilesSelected(List<T> tiles) {
    List<ITileGridExtension<T, ? extends AbstractTileGrid>> extensions = getAllExtensions();
    TilesSelectedChain<T> chain = new TilesSelectedChain<>(extensions);
    chain.execTilesSelected(tiles);
  }

  protected class P_PropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (event.getPropertyName().equals(PROP_SELECTED_TILES)) {
        @SuppressWarnings("unchecked")
        List<T> tiles = (List<T>) event.getNewValue();
        try {
          interceptTilesSelected(tiles);
        }
        catch (Exception t) {
          BEANS.get(ExceptionHandler.class).handle(t);
        }
      }
    }
  }

}
