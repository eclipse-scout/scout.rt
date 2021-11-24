/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile;

import static java.lang.String.valueOf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.extension.ui.tile.ITileAccordionExtension;
import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TileActionChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TileClickChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("e1e96659-f922-45c8-b350-78f9de059a83")
public abstract class AbstractTileAccordion<T extends ITile> extends AbstractAccordion implements ITileAccordion<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTileAccordion.class);

  private List<ITileFilter<T>> m_tileFilters;
  private Map<Object, ITileAccordionGroupManager<T>> m_groupManagers;
  private ITileAccordionGroupManager<T> m_groupManager;
  private boolean m_selectionUpdateLocked = false;
  private List<IGroup> m_staticGroups = new ArrayList<>();
  private final FastListenerList<TileGridListener> m_listenerList;
  private final ObjectExtensions<AbstractTileAccordion, ITileAccordionExtension<T, ? extends AbstractTileAccordion>> m_objectExtensions;

  public AbstractTileAccordion() {
    this(true);
  }

  public AbstractTileAccordion(boolean callInitializer) {
    super(false);
    m_tileFilters = new ArrayList<>();
    m_groupManagers = new HashMap<>();
    m_listenerList = new FastListenerList<>();
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
    setTextFilterEnabled(getConfiguredTextFilterEnabled());
    setGroupManager(new DefaultGroupManager<>());
    setComparator(new DefaultComparator());

    // Copy properties from default grid to accordion
    ITileGrid<T> defaultGrid = getTileGrid(getDefaultGroup());
    setSelectable(defaultGrid.isSelectable());
    setMultiSelect(defaultGrid.isMultiSelect());
    setWithPlaceholders(defaultGrid.isWithPlaceholders());
    setVirtual(defaultGrid.isVirtual());
    setGridColumnCount(defaultGrid.getGridColumnCount());
    setTileGridLayoutConfig(defaultGrid.getLayoutConfig());
    setTileComparator(defaultGrid.getComparator());
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredTextFilterEnabled() {
    return false;
  }

  @Override
  protected boolean getConfiguredExclusiveExpand() {
    return false;
  }

  @Override
  public void addTile(T tile) {
    addTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void addTiles(List<T> tilesToAdd) {
    List<T> tiles = new ArrayList<>(getTiles());
    tiles.addAll(tilesToAdd);
    setTiles(tiles);
  }

  protected IGroup getOrCreateGroupByTile(T tile) {
    IGroup group = getGroupByTile(tile);
    if (group != null) {
      return group;
    }
    GroupTemplate template = m_groupManager.createGroupForTile(tile);
    if (template == null) {
      group = getDefaultGroup();
      if (group == null) {
        throw new IllegalStateException("No default group found!");
      }
      return group;
    }
    group = createGroup();
    addGroup(group);
    adaptGroup(group, template);
    return group;
  }

  @Override
  public IGroup getGroupByTile(T tile) {
    Object groupId = m_groupManager.getGroupIdByTile(tile);
    return getGroupById(groupId);
  }

  @Override
  public IGroup getGroupById(Object groupId) {
    return getGroups().stream()
        .filter(group -> ObjectUtility.equals(groupId, group.getGroupId()))
        .findFirst()
        .orElse(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <G extends IGroup> G getDefaultGroup() {
    return (G) getGroupById(DefaultGroupManager.GROUP_ID_DEFAULT);
  }

  @Override
  public void setTiles(List<T> tiles) {
    tiles = ObjectUtility.nvl(tiles, new ArrayList<>());
    Map<IGroup, List<T>> map = new HashMap<>();
    for (IGroup group : getGroupsInternal()) {
      map.put(group, new ArrayList<>());
    }
    for (T tile : tiles) {
      IGroup group = getOrCreateGroupByTile(tile);
      map.putIfAbsent(group, new ArrayList<>());
      map.get(group).add(tile);
    }
    for (Entry<IGroup, List<T>> entry : map.entrySet()) {
      IGroup group = entry.getKey();
      getTileGrid(group).setTiles(entry.getValue());
      if (entry.getValue().size() == 0 && !m_staticGroups.contains(group)) {
        // Delete obsolete groups but never the static ones
        deleteGroup(group);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected ITileGrid<T> getTileGrid(IGroup group) {
    return (ITileGrid) group.getBody();
  }

  /**
   * Adds and activates the given group manager.
   */
  @Override
  public void setGroupManager(ITileAccordionGroupManager<T> groupManager) {
    addGroupManager(groupManager);
    activateGroupManager(groupManager.getId());
  }

  /**
   * @return the active group manager
   */
  @Override
  public ITileAccordionGroupManager<T> getGroupManager() {
    return m_groupManager;
  }

  @Override
  public void addGroupManager(ITileAccordionGroupManager<T> groupManager) {
    m_groupManagers.put(groupManager.getId(), groupManager);
  }

  @Override
  public void removeGroupManager(ITileAccordionGroupManager<T> groupManager) {
    m_groupManagers.remove(groupManager.getId());
    // when the current group handler is removed, activate the default group manager
    if (groupManager == m_groupManager) {
      activateGroupManager(DefaultGroupManager.ID);
    }
  }

  /**
   * Activates a group manager that matches the given ID.
   */
  @Override
  public void activateGroupManager(Object groupManagerId) {
    ITileAccordionGroupManager<T> groupManager = m_groupManagers.get(groupManagerId);
    if (groupManager == null) {
      throw new IllegalArgumentException("No group manager registered for ID " + groupManagerId);
    }
    if (groupManager == m_groupManager) {
      // manager already active, do nothing
      return;
    }
    List<T> allTiles = getTiles();

    if (m_groupManager instanceof DefaultGroupManager) {
      // Delete default group when switching group manager (-> changing from ungrouped to grouped)
      // This makes sure tiles from the default group are not removed with an animation while the same tiles are already added to a new group
      IGroup defaultGroup = getDefaultGroup();
      if (defaultGroup != null) {
        deleteGroup(defaultGroup);
        m_staticGroups.remove(defaultGroup);
      }
    }
    m_groupManager = groupManager;

    // Each group manager may define a set of static groups which are shown even if they do not contain any tiles
    // These static groups may be reused when activating another group manager
    List<? extends IGroup> currentGroups = new ArrayList<>(m_staticGroups);

    // We always add a default group which is used when the DefaultGroupManager is active or
    // when a group manager is active and one or more tile could not be put into a matching group
    // thus this group acts as a "catch-all" for tiles without a group.
    GroupTemplate defaultGroup = createDefaultGroupTemplate();
    List<GroupTemplate> groupTemplates = m_groupManager.createGroups();
    List<GroupTemplate> requiredGroups = new ArrayList<>(groupTemplates);
    requiredGroups.add(defaultGroup);
    int currentSize = currentGroups.size();
    int requiredSize = requiredGroups.size();

    // delete all groups we don't need anymore
    if (currentSize > requiredSize) {
      for (int i = requiredSize; i < currentSize; i++) {
        deleteGroup(currentGroups.get(i));
        m_staticGroups.remove(currentGroups.get(i));
      }
    }

    // add the missing groups
    if (currentSize < requiredSize) {
      for (int i = currentSize; i < requiredSize; i++) {
        IGroup group = createGroup();
        addGroup(group);
        m_staticGroups.add(group);
      }
    }

    // Make sure that the all groups have the properties set as defined by the requiredGroups
    // Note: since we re-use existing groups we might throw away some groups returned by createGroups
    // Order of current groups is important here to make sure templates are applied to the correct groups
    currentGroups = getGroupsInternal().stream().filter(g -> m_staticGroups.contains(g)).collect(Collectors.toList());
    for (int i = 0; i < requiredSize; i++) {
      IGroup group = currentGroups.get(i);
      GroupTemplate groupTemplate = requiredGroups.get(i);
      adaptGroup(group, groupTemplate);
    }

    setTiles(allTiles);
    sort();
    updateDefaultGroupState();
  }

  protected IGroup createGroup() {
    OrderedCollection<IGroup> groups = new OrderedCollection<>();
    injectGroupsInternal(groups);
    if (groups.size() != 1) {
      throw new IllegalStateException("Must have excatly one group as inner class, but there are " + groups.size() + " groups");
    }
    return groups.get(0);
  }

  @Override
  protected void addGroupInternal(IGroup group) {
    super.addGroupInternal(group);
    handleGroupCollapsedChange(group);
    group.addPropertyChangeListener(new P_GroupPropertyChangeListener());
    getTileGrid(group).addPropertyChangeListener(new P_TileGridPropertyChangeListener(group));
    getTileGrid(group).addTileGridListener(new P_TileGridListener());
  }

  protected void adaptGroup(IGroup group, GroupTemplate template) {
    group.setTitle(template.getTitle());
    group.setGroupId(template.getGroupId());
    group.setIconId(template.getIconId());
    group.setCssClass(template.getCssClass());
    group.setCollapsed(template.isCollapsed());
    group.setHeaderVisible(template.isHeaderVisible());

    // Don't apply properties for the default grid while initConfig is running to not override the default values
    if (getDefaultGroup() != group || isInitConfigDone()) {
      applyTileGridProperties(group);
    }
  }

  protected void applyTileGridProperties(IGroup group) {
    // Every tile grid uses the same values for the following properties.
    // If it is required to have different properties for each tile grid one may override applyTileGridProperties and handle them individually.
    ITileGrid<T> tileGrid = getTileGrid(group);
    tileGrid.setSelectable(isSelectable());
    tileGrid.setMultiSelect(isMultiSelect());
    tileGrid.setGridColumnCount(getGridColumnCount());
    tileGrid.setWithPlaceholders(isWithPlaceholders());
    tileGrid.setVirtual(isVirtual());
    tileGrid.setLayoutConfig(getTileGridLayoutConfig());
    tileGrid.setComparator(getTileComparator());
    for (ITileFilter<T> filter : m_tileFilters) {
      tileGrid.addFilter(filter);
    }
  }

  protected GroupTemplate createDefaultGroupTemplate() {
    return new GroupTemplate(DefaultGroupManager.GROUP_ID_DEFAULT, TEXTS.get("NotGrouped"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ITileGrid<T>> getTileGrids() {
    List<ITileGrid<T>> tileGrids = new ArrayList<>();
    for (IGroup group : getGroups()) {
      tileGrids.add((ITileGrid) group.getBody());
    }
    return tileGrids;
  }

  @Override
  public Stream<T> streamTiles() {
    return getTiles().stream();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> getTiles() {
    List<T> allTiles = new ArrayList<>();
    for (ITileGrid<T> tiles : getTileGrids()) {
      allTiles.addAll(((AbstractTileGrid) tiles).getTilesInternal());
    }
    return allTiles;
  }

  @Override
  public int getTileCount() {
    return getTileGrids()
        .stream()
        .mapToInt(ITileGrid::getTileCount)
        .sum();
  }

  @Override
  public void addTileFilter(ITileFilter<T> filter) {
    m_tileFilters.add(filter);
    getTileGrids().forEach(tileGrid -> tileGrid.addFilter(filter));
  }

  @Override
  public void removeTileFilter(ITileFilter<T> filter) {
    m_tileFilters.remove(filter);
    getTileGrids().forEach(tileGrid -> tileGrid.removeFilter(filter));
  }

  @Override
  public void deleteAllTiles() {
    setTiles(new ArrayList<>());
  }

  @Override
  public void deleteTiles(Collection<T> tiles) {
    tiles.forEach(this::deleteTile);
  }

  @Override
  public void deleteTile(T tile) {
    deleteTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void deleteTiles(List<T> tilesToDelete) {
    List<T> tiles = new ArrayList<>(getTiles());
    tiles.removeAll(tilesToDelete);
    setTiles(tiles);
  }

  @Override
  public void filterTiles() {
    getTileGrids().forEach(ITileGrid::filter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> getFilteredTiles() {
    List<T> allTiles = new ArrayList<>();
    for (ITileGrid<T> tiles : getTileGrids()) {
      allTiles.addAll(((AbstractTileGrid) tiles).getFilteredTilesInternal());
    }
    return allTiles;
  }

  @Override
  public int getFilteredTileCount() {
    return getTileGrids()
        .stream()
        .mapToInt(ITileGrid::getFilteredTileCount)
        .sum();
  }

  @Override
  public void setTileComparator(Comparator<T> comparator) {
    propertySupport.setProperty(PROP_TILE_COMPARATOR, comparator);
    getTileGrids().forEach(tileGrid -> tileGrid.setComparator(comparator));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Comparator<T> getTileComparator() {
    return (Comparator<T>) propertySupport.getProperty(PROP_TILE_COMPARATOR);
  }

  /**
   * Returns the comparator of the active group manager if there is one, otherwise returns {@link #getComparator()}.
   * This means the comparator of the group manager has higher priority.
   */
  @Override
  protected Comparator<? extends IGroup> resolveComparator() {
    if (getGroupManager() != null && getGroupManager().getComparator() != null) {
      return getGroupManager().getComparator();
    }
    return super.getComparator();
  }

  @Override
  public T getSelectedTile() {
    for (ITileGrid<T> tileGrid : getTileGrids()) {
      T selectedTile = tileGrid.getSelectedTile();
      if (selectedTile != null) {
        return selectedTile;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> getSelectedTiles() {
    List<T> selectedTiles = new ArrayList<>();
    for (ITileGrid<T> tiles : getTileGrids()) {
      selectedTiles.addAll(((AbstractTileGrid) tiles).getSelectedTilesInternal());
    }
    return selectedTiles;
  }

  @Override
  public int getSelectedTileCount() {
    return getTileGrids()
        .stream()
        .mapToInt(ITileGrid::getSelectedTileCount)
        .sum();
  }

  @Override
  public void selectTile(T tile) {
    selectTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void selectTiles(List<T> tiles) {
    // Split tiles into separate lists for each group
    // Process tiles in reverse order so that selectTiles(tile0, tile1) with multiSelect = false
    // will lead to the same result as done with AbstractTiles directly
    Map<IGroup, List<T>> tilesPerGroup = groupTilesReverse(tiles);

    // Deselect tiles from groups which don't contain any of the newly selected tiles
    List<? extends IGroup> groups = getGroups();
    groups.removeAll(tilesPerGroup.keySet());
    for (IGroup group : groups) {
      getTileGrid(group).deselectAllTiles();
    }

    // Select the tiles in the the corresponding tile grids
    for (Entry<IGroup, List<T>> entry : tilesPerGroup.entrySet()) {
      getTileGrid(entry.getKey()).selectTiles(entry.getValue());
    }
  }

  @Override
  public void selectAllTiles() {
    getTileGrids().forEach(ITileGrid::selectAllTiles);
  }

  @Override
  public void deselectTile(T tile) {
    deselectTiles(CollectionUtility.arrayList(tile));
  }

  @Override
  public void deselectTiles(List<T> tiles) {
    getTileGrids().forEach(tileGrid -> tileGrid.deselectTiles(tiles));
  }

  @Override
  public void deselectAllTiles() {
    getTileGrids().forEach(ITileGrid::deselectAllTiles);
  }

  protected Map<IGroup, List<T>> groupTilesReverse(List<T> tiles) {
    Map<IGroup, List<T>> tilesPerGroup = new LinkedHashMap<>();
    // Split tiles to select into separate lists for each group
    for (int i = tiles.size() - 1; i >= 0; i--) {
      T tile = tiles.get(i);
      IGroup group = getGroupByTile(tile);
      tilesPerGroup.computeIfAbsent(group, t -> new ArrayList<>()).add(tile);
    }
    return tilesPerGroup;
  }

  @Override
  public void setGridColumnCount(int gridColumnCount) {
    propertySupport.setPropertyInt(PROP_GRID_COLUMN_COUNT, gridColumnCount);
    getTileGrids().forEach(tileGrid -> tileGrid.setGridColumnCount(gridColumnCount));
  }

  @Override
  public int getGridColumnCount() {
    return propertySupport.getPropertyInt(PROP_GRID_COLUMN_COUNT);
  }

  @Override
  public void setSelectable(boolean selectable) {
    propertySupport.setPropertyBool(PROP_SELECTABLE, selectable);
    getTileGrids().forEach(tileGrid -> tileGrid.setSelectable(selectable));
  }

  @Override
  public boolean isSelectable() {
    return propertySupport.getPropertyBool(PROP_SELECTABLE);
  }

  @Override
  public void setMultiSelect(boolean multiSelect) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, multiSelect);
    getTileGrids().forEach(tileGrid -> tileGrid.setMultiSelect(multiSelect));
  }

  @Override
  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  @Override
  public void setWithPlaceholders(boolean withPlaceholders) {
    propertySupport.setPropertyBool(PROP_WITH_PLACEHOLDERS, withPlaceholders);
    getTileGrids().forEach(tileGrid -> tileGrid.setWithPlaceholders(withPlaceholders));
  }

  @Override
  public boolean isWithPlaceholders() {
    return propertySupport.getPropertyBool(PROP_WITH_PLACEHOLDERS);
  }

  @Override
  public void setTileGridLayoutConfig(TileGridLayoutConfig layoutConfig) {
    propertySupport.setProperty(PROP_TILE_GRID_LAYOUT_CONFIG, layoutConfig);
    getTileGrids().forEach(tileGrid -> tileGrid.setLayoutConfig(layoutConfig));
  }

  @Override
  public TileGridLayoutConfig getTileGridLayoutConfig() {
    return (TileGridLayoutConfig) propertySupport.getProperty(PROP_TILE_GRID_LAYOUT_CONFIG);
  }

  @Override
  public void setVirtual(boolean virtual) {
    propertySupport.setPropertyBool(PROP_VIRTUAL, virtual);
    getTileGrids().forEach(tileGrid -> tileGrid.setVirtual(virtual));
  }

  @Override
  public boolean isVirtual() {
    return propertySupport.getPropertyBool(PROP_VIRTUAL);
  }

  @Override
  public void setTextFilterEnabled(boolean textFilterEnabled) {
    propertySupport.setPropertyBool(PROP_TEXT_FILTER_ENABLED, textFilterEnabled);
  }

  @Override
  public boolean isTextFilterEnabled() {
    return propertySupport.getPropertyBool(PROP_TEXT_FILTER_ENABLED);
  }

  protected void handleSelectedTilesChange(IGroup changedGroup, PropertyChangeEvent event) {
    if (m_selectionUpdateLocked) {
      return;
    }
    ITileGrid<T> tileGrid = getTileGrid(changedGroup);
    if (tileGrid.getSelectedTileCount() > 0 && changedGroup.isCollapsed()) {
      // Do not allow selection in a collapsed group (breaks keyboard navigation and is confusing for the user if invisible tiles are selected)
      tileGrid.deselectAllTiles();
      return;
    }
    List<? extends ITile> selectedTiles = tileGrid.getSelectedTiles();
    if (!isMultiSelect() && selectedTiles.size() > 0) {
      m_selectionUpdateLocked = true;
      try {
        // make sure only one group has selected tiles
        for (IGroup group : getGroupsInternal()) {
          if (group != changedGroup) {
            getTileGrid(group).deselectAllTiles();
          }
        }
      }
      finally {
        m_selectionUpdateLocked = false;
      }
    }
    List<T> newSelectedTiles = getSelectedTiles();
    interceptTilesSelected(newSelectedTiles);
    propertySupport.setProperty(PROP_SELECTED_TILES, newSelectedTiles);
  }

  protected void handleFilteredTilesChange(IGroup changedGroup, PropertyChangeEvent event) {
    updateGroupTitleSuffix(changedGroup);
    updateDefaultGroupState();
  }

  protected void updateDefaultGroupState() {
    // When DGM is active, we never show the header
    IGroup defaultGroup = getDefaultGroup();
    ITileGrid defaultTileGrid = getTileGrid(defaultGroup);
    if (m_groupManager instanceof DefaultGroupManager) {
      setPropertyPreservingOldValue(defaultGroup, "oldHeaderVisible", defaultGroup::isHeaderVisible, defaultGroup::setHeaderVisible);
      setPropertyPreservingOldValue(defaultGroup, "oldCollapsible", defaultGroup::isCollapsible, defaultGroup::setCollapsible);
    }
    else {
      // Make default group invisible if it has no tiles
      boolean hasTiles = defaultTileGrid.getFilteredTileCount() > 0;
      setPropertyIncludingOldValue(defaultGroup, "oldHeaderVisible", hasTiles, defaultGroup::setHeaderVisible);
      setPropertyIncludingOldValue(defaultGroup, "oldCollapsible", hasTiles, defaultGroup::setCollapsible);
    }
    // Collapse default group if it has no tiles
    defaultGroup.setCollapsed(defaultTileGrid.getFilteredTileCount() == 0);
  }

  protected void setPropertyPreservingOldValue(IGroup group, String propertyName, Supplier<Object> supplier, Consumer<Boolean> consumer) {
    group.setProperty(propertyName, supplier.get());
    consumer.accept(false);
  }

  protected void setPropertyIncludingOldValue(IGroup group, String propertyName, boolean hasTiles, Consumer<Boolean> consumer) {
    Boolean oldValue = Optional.ofNullable(group.getProperty(propertyName))
        .map(o -> (Boolean) o)
        .orElse(Boolean.TRUE);
    consumer.accept(oldValue && hasTiles);
  }

  protected void updateGroupTitleSuffix(IGroup group) {
    group.setTitleSuffix(createGroupTitleSuffixLabel(group));
  }

  /**
   * Creates the label displayed as suffix in the title of the group-header. The default implementation returns the
   * count of tiles and some braces. Example: "(19)".
   * <p>
   * Override this method if you need another suffix, or change the count.
   */
  protected String createGroupTitleSuffixLabel(IGroup group) {
    int numFilteredTiles = getTileGrid(group).getFilteredTileCount();
    return valueOf(numFilteredTiles);
  }

  @Override
  public IFastListenerList<TileGridListener> tileGridListeners() {
    return m_listenerList;
  }

  protected void fireTileGridEventInternal(TileGridEvent e) {
    tileGridListeners().list().forEach(listener -> listener.tileGridChanged(e));
  }

  @SuppressWarnings("unchecked")
  protected void handleTileGridChanged(TileGridEvent e) {
    switch (e.getType()) {
      case TileGridEvent.TYPE_TILE_CLICK:
        interceptTileClick((T) e.getTile(), e.getMouseButton());
        fireTileGridEventInternal(e);
        break;
      case TileGridEvent.TYPE_TILE_ACTION:
        interceptTileAction((T) e.getTile());
        fireTileGridEventInternal(e);
        break;
      default:
        LOG.debug("Unhandled event from TileGrid received " + e);
    }
  }

  public class P_TileGridListener implements TileGridListener {

    @Override
    public void tileGridChanged(TileGridEvent event) {
      handleTileGridChanged(event);
    }
  }

  public class P_TileGridPropertyChangeListener implements PropertyChangeListener {

    private IGroup m_group;

    public P_TileGridPropertyChangeListener(IGroup group) {
      m_group = group;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (ITileGrid.PROP_FILTERED_TILES.equals(evt.getPropertyName())) {
        handleFilteredTilesChange(m_group, evt);
      }
      else if (ITileGrid.PROP_SELECTED_TILES.equals(evt.getPropertyName())) {
        handleSelectedTilesChange(m_group, evt);
      }
    }
  }

  protected void handleGroupCollapsedChange(IGroup group) {
    if (group.isCollapsed()) {
      // Deselect tiles of a collapsed group -> actions on invisible elements is confusing, and key strokes only operate on visible elements, too
      getTileGrid(group).deselectAllTiles();
    }
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

  /**
   * Called whenever the tile is clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param tile
   *          that was clicked
   */
  @ConfigOperation
  @Order(110)
  protected void execTileClick(T tile, MouseButton mouseButton) {
  }

  /**
   * Called whenever the tile is double clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param tile
   *          to perform the action for
   */
  @ConfigOperation
  @Order(120)
  protected void execTileAction(T tile) {
  }

  @Override
  public final List<ITileAccordionExtension<T, ? extends AbstractTileAccordion>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <E extends IExtension<?>> E getExtension(Class<E> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected ITileAccordionExtension<T, ? extends AbstractTileAccordion> createLocalExtension() {
    return new LocalTileAccordionExtension<>(this);
  }

  protected static class LocalTileAccordionExtension<T extends ITile, TILES extends AbstractTileAccordion<T>> extends AbstractExtension<TILES> implements ITileAccordionExtension<T, TILES> {

    public LocalTileAccordionExtension(TILES owner) {
      super(owner);
    }

    @Override
    public void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles) {
      getOwner().execTilesSelected(tiles);
    }

    @Override
    public void execTileClick(TileClickChain<T> chain, T tile, MouseButton mouseButton) {
      getOwner().execTileClick(tile, mouseButton);
    }

    @Override
    public void execTileAction(TileActionChain<T> chain, T tile) {
      getOwner().execTileAction(tile);
    }

  }

  protected final void interceptTilesSelected(List<T> tiles) {
    List<ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions = getAllExtensions();
    TilesSelectedChain<T> chain = new TilesSelectedChain<>(extensions);
    chain.execTilesSelected(tiles);
  }

  protected final void interceptTileClick(T tile, MouseButton mouseButton) {
    List<ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions = getAllExtensions();
    TileClickChain<T> chain = new TileClickChain<>(extensions);
    chain.execTileClick(tile, mouseButton);
  }

  protected final void interceptTileAction(T tile) {
    List<ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions = getAllExtensions();
    TileActionChain<T> chain = new TileActionChain<>(extensions);
    chain.execTileAction(tile);
  }

  protected class P_GroupPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IGroup.PROP_COLLAPSED.equals(evt.getPropertyName())) {
        handleGroupCollapsedChange((IGroup) evt.getSource());
      }
    }
  }

  public static class DefaultComparator implements Comparator<IGroup>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(IGroup group1, IGroup group2) {
      // Default group is always a the end
      if (DefaultGroupManager.GROUP_ID_DEFAULT.equals(group1.getGroupId())) {
        return 1;
      }
      if (DefaultGroupManager.GROUP_ID_DEFAULT.equals(group2.getGroupId())) {
        return -1;
      }
      return 0;
    }
  }

}
