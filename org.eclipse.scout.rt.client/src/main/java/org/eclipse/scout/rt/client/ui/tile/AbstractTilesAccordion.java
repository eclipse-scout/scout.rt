/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.TEXTS;

@ClassId("e1e96659-f922-45c8-b350-78f9de059a83")
public abstract class AbstractTilesAccordion<T extends ITile> extends AbstractAccordion {

  public static final String PROP_SHOW_FILTER_COUNT = "showFilterCount";
  public static final String PROP_SELECTED_TILES = "selectedTiles";

  private List<ITileFilter> m_tileFilters;
  private Map<Object, ITilesAccordionGroupManager<T>> m_groupManagers;
  private ITilesAccordionGroupManager<T> m_groupManager;
  private boolean m_selectionUpdateLocked = false;
  private List<IGroup> m_staticGroups = new ArrayList<>();

  public AbstractTilesAccordion() {
    this(true);
  }

  public AbstractTilesAccordion(boolean callInitializer) {
    super(false);
    m_tileFilters = new ArrayList<>();
    m_groupManagers = new HashMap<>();
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setShowFilterCount(getConfiguredShowFilterCount());
    setGroupManager(new DefaultGroupManager<T>());
    setComparator(new DefaultComparator());
    IGroup firstGroup = getDefaultGroup();
    getTileGrid(firstGroup).addPropertyChangeListener(new P_FilteredTilesListener(firstGroup));
  }

  @Override
  protected boolean getConfiguredExclusiveExpand() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredShowFilterCount() {
    return false;
  }

  public void addTile(T tile) {
    addTiles(CollectionUtility.arrayList(tile));
  }

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

  public IGroup getGroupByTile(T tile) {
    Object groupId = m_groupManager.getGroupIdByTile(tile);
    return getGroupById(groupId);
  }

  protected IGroup getGroupById(Object groupId) {
    return getGroups().stream()
        .filter(group -> ObjectUtility.equals(groupId, group.getGroupId()))
        .findFirst()
        .orElse(null);
  }

  @SuppressWarnings("unchecked")
  public <G extends IGroup> G getDefaultGroup() {
    return (G) getGroupById(DefaultGroupManager.GROUP_ID_DEFAULT);
  }

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
      getTileGrid(entry.getKey()).setTiles(entry.getValue());
      if (entry.getValue().size() == 0 && !m_staticGroups.contains(group)) {
        // Delete obsolete groups but never the static ones
        deleteGroup(group);
      }
    }
    updateGroupStates();
  }

  protected void updateGroupStates() {
    updateDefaultGroupVisible();
    updateCollapseStateOfGroups();
  }

  protected void updateDefaultGroupVisible() {
    // When DGM is active, we never show the header
    IGroup defaultGroup = getDefaultGroup();
    if (m_groupManager instanceof DefaultGroupManager) {
      defaultGroup.setHeaderVisible(false);
    }
    else {
      // Make the default group invisible, when it has no tiles at all
      ITiles defaultTiles = getTileGrid(defaultGroup);
      defaultGroup.setHeaderVisible(defaultTiles.getTileCount() > 0);
    }
  }

  protected void updateCollapseStateOfGroups() {
    for (IGroup group : getGroupsInternal()) {
      boolean collapsed = getTileGrid(group).getTileCount() == 0;
      group.setCollapsed(collapsed);
    }
  }

  @SuppressWarnings("unchecked")
  protected ITiles<T> getTileGrid(IGroup group) {
    return (ITiles) group.getBody();
  }

  public boolean isShowFilterCount() {
    return propertySupport.getPropertyBool(PROP_SHOW_FILTER_COUNT);
  }

  public void setShowFilterCount(boolean showFilterCount) {
    propertySupport.setPropertyBool(PROP_SHOW_FILTER_COUNT, showFilterCount);
  }

  /**
   * Adds and activates the given group manager.
   */
  public void setGroupManager(ITilesAccordionGroupManager<T> groupManager) {
    addGroupManager(groupManager);
    activateGroupManager(groupManager.getId());
  }

  /**
   * @returns the active group manager
   */
  public ITilesAccordionGroupManager<T> getGroupManager() {
    return m_groupManager;
  }

  public void addGroupManager(ITilesAccordionGroupManager<T> groupManager) {
    m_groupManagers.put(groupManager.getId(), groupManager);
  }

  public void removeGroupManager(ITilesAccordionGroupManager<T> groupManager) {
    m_groupManagers.remove(groupManager.getId());
    // when the current group handler is removed, activate the default group manager
    if (groupManager == m_groupManager) {
      activateGroupManager(DefaultGroupManager.ID);
    }
  }

  /**
   * Activates a group manager that matches the given ID.
   */
  public void activateGroupManager(Object groupManagerId) {
    ITilesAccordionGroupManager<T> groupManager = m_groupManagers.get(groupManagerId);
    if (groupManager == null) {
      throw new IllegalArgumentException("No group manager registered for ID " + groupManagerId);
    }
    if (groupManager == m_groupManager) {
      // manager already active, do nothing
      return;
    }
    m_groupManager = groupManager;

    List<T> allTiles = getTiles();
    // Each group manager may define a set of static groups which are shown even if they do not contain any tiles
    // These static groups may be reused when activating another group manager
    List<? extends IGroup> currentGroups = new ArrayList<>(m_staticGroups);

    // We always add a default group which is used when the DefaultGroupManager is active or
    // when a group manager is active and one or more tile could not be put into a matching group
    // thus this group acts as a "catch-all" for tiles without a group.
    GroupTemplate defaultGroup = createDefaultGroupTemplate();
    List<GroupTemplate> requiredGroups = new ArrayList<GroupTemplate>();
    List<GroupTemplate> groupTemplates = m_groupManager.createGroups();
    requiredGroups.addAll(groupTemplates);
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

    for (ITileFilter filter : m_tileFilters) {
      addFilterToAllTileGrids(filter);
    }
    sort();
  }

  protected IGroup createGroup() {
    OrderedCollection<IGroup> groups = new OrderedCollection<>();
    injectGroupsInternal(groups);
    if (groups.size() != 1) {
      throw new IllegalStateException("Must have excatly one group as inner class, but there are " + groups.size() + " groups");
    }
    IGroup group = groups.get(0);
    getTileGrid(group).addPropertyChangeListener(new P_FilteredTilesListener(group));
    return group;
  }

  protected void adaptGroup(IGroup group, GroupTemplate template) {
    group.setTitle(template.getTitle());
    group.setGroupId(template.getGroupId());
    group.setCssClass(template.getCssClass());
    group.setCollapsed(template.isCollapsed());
    group.setHeaderVisible(template.isHeaderVisible());
    if (group != getDefaultGroup()) {
      // The default group maintains the properties to be set on each tile grid. Every tile grid uses the same values.
      // If it is required to have different properties for each tile grid one may override applyTileGridProperties and handle them individually.
      applyTileGridProperties(group);
    }
  }

  protected void applyTileGridProperties(IGroup group) {
    ITiles<T> defaultGrid = getTileGrid(getDefaultGroup());
    getTileGrid(group).setSelectable(defaultGrid.isSelectable());
    getTileGrid(group).setMultiSelect(defaultGrid.isMultiSelect());
    getTileGrid(group).setGridColumnCount(defaultGrid.getGridColumnCount());
    getTileGrid(group).setWithPlaceholders(defaultGrid.isWithPlaceholders());
    getTileGrid(group).setComparator(defaultGrid.getComparator());
    getTileGrid(group).setLayoutConfig(defaultGrid.getLayoutConfig());
  }

  protected GroupTemplate createDefaultGroupTemplate() {
    return new GroupTemplate(DefaultGroupManager.GROUP_ID_DEFAULT, TEXTS.get("NotGrouped"));
  }

  @SuppressWarnings("unchecked")
  public List<ITiles<T>> getTileGrids() {
    List<ITiles<T>> tileGrids = new ArrayList<>();
    for (IGroup group : getGroups()) {
      tileGrids.add((ITiles) group.getBody());
    }
    return tileGrids;
  }

  public Stream<T> streamTiles() {
    return getTiles().stream();
  }

  @SuppressWarnings("unchecked")
  public List<T> getTiles() {
    List<T> allTiles = new ArrayList<>();
    for (ITiles<T> tiles : getTileGrids()) {
      allTiles.addAll(((AbstractTiles) tiles).getTilesInternal());
    }
    return allTiles;
  }

  public int getTileCount() {
    return getTileGrids()
        .stream()
        .mapToInt(ITiles::getTileCount)
        .sum();
  }

  public void addTilesFilter(ITileFilter filter) {
    m_tileFilters.add(filter);
    addFilterToAllTileGrids(filter);
  }

  protected void addFilterToAllTileGrids(ITileFilter filter) {
    getTileGrids().forEach(tileGrid -> tileGrid.addFilter(filter));
  }

  public void removeTilesFilter(ITileFilter filter) {
    m_tileFilters.remove(filter);
    removeFilterFromAllTileGrids(filter);
  }

  protected void removeFilterFromAllTileGrids(ITileFilter filter) {
    getTileGrids().forEach(tileGrid -> tileGrid.removeFilter(filter));
  }

  public void deleteAllTiles() {
    setTiles(new ArrayList<>());
  }

  public void deleteTiles(Collection<T> tiles) {
    tiles.forEach(tile -> this.deleteTile(tile));
  }

  public void deleteTile(T tile) {
    deleteTiles(CollectionUtility.arrayList(tile));
  }

  public void deleteTiles(List<T> tilesToDelete) {
    List<T> tiles = new ArrayList<>(getTiles());
    tiles.removeAll(tilesToDelete);
    setTiles(tiles);
  }

  public void filterTiles() {
    getTileGrids().forEach(ITiles::filter);
  }

  public void setTileComparator(Comparator<T> comparator) {
    getTileGrids().forEach(tileGrid -> tileGrid.setComparator(comparator));
  }

  /**
   * Returns the comparator of the active group manager if there is one, otherwise returns {@link #getComparator()}. This
   * means the comparator of the group manager has higher priority.
   */
  @Override
  public Comparator<? extends IGroup> resolveComparator() {
    if (getGroupManager() != null && getGroupManager().getComparator() != null) {
      return getGroupManager().getComparator();
    }
    return super.getComparator();
  }

  public T getSelectedTile() {
    for (ITiles<T> tileGrid : getTileGrids()) {
      if (tileGrid.getSelectedTile() != null) {
        return tileGrid.getSelectedTile();
      }
    }
    return null;
  }

  public List<T> getSelectedTiles() {
    for (ITiles<T> tileGrid : getTileGrids()) {
      List<T> selectedTiles = tileGrid.getSelectedTiles();
      if (selectedTiles != null && selectedTiles.size() != 0) {
        return selectedTiles;
      }
    }
    return Collections.emptyList();
  }

  public void selectTile(T tile) {
    selectTiles(CollectionUtility.arrayList(tile));
  }

  public void selectTiles(List<T> tiles) {
    // Split tiles into separate lists for each group
    Map<IGroup, List<T>> tilesPerGroup = groupTiles(tiles);

    // Delegate to the corresponding tile grids
    for (Entry<IGroup, List<T>> entry : tilesPerGroup.entrySet()) {
      getTileGrid(entry.getKey()).selectTiles(entry.getValue());
    }
  }

  public void selectAllTiles() {
    getTileGrids().forEach(ITiles::selectAllTiles);
  }

  public void deselectTile(T tile) {
    deselectTiles(CollectionUtility.arrayList(tile));
  }

  public void deselectTiles(List<T> tiles) {
    getTileGrids().forEach(tileGrid -> tileGrid.deselectTiles(tiles));
  }

  public void deselectAllTiles() {
    getTileGrids().forEach(ITiles::deselectAllTiles);
  }

  protected Map<IGroup, List<T>> groupTiles(List<T> tiles) {
    Map<IGroup, List<T>> tilesPerGroup = new HashMap<>();
    // Split tiles to select into separate lists for each group
    for (T tile : tiles) {
      IGroup group = getGroupByTile(tile);
      tilesPerGroup.computeIfAbsent(group, t -> new ArrayList<>()).add(tile);
    }
    return tilesPerGroup;
  }

  public void setGridColumnCount(int gridColumnCount) {
    getTileGrids().forEach(tileGrid -> tileGrid.setGridColumnCount(gridColumnCount));
  }

  /**
   * @return the value of {@link ITiles#getGridColumnCount()} of the first tile grid assuming that all tile grids use the
   *         same column count
   */
  public int getGridColumnCount() {
    return getTileGrids().get(0).getGridColumnCount();
  }

  public void setSelectable(boolean selectable) {
    getTileGrids().forEach(tileGrid -> tileGrid.setSelectable(selectable));
  }

  /**
   * @return the value of {@link ITiles#isSelectable()} of the first tile grid assuming that all tile grids use the same
   *         value
   */
  public boolean isSelectable() {
    return getTileGrids().get(0).isSelectable();
  }

  public void setMultiSelect(boolean multiSelect) {
    getTileGrids().forEach(tileGrid -> tileGrid.setMultiSelect(multiSelect));
  }

  /**
   * @return the value of {@link ITiles#isMultiSelect()} of the first tile grid assuming that all tile grids use the same
   *         value
   */
  public boolean isMultiSelect() {
    return getTileGrids().get(0).isMultiSelect();
  }

  public void setWithPlaceholders(boolean withPlaceholders) {
    getTileGrids().forEach(tileGrid -> tileGrid.setWithPlaceholders(withPlaceholders));
  }

  /**
   * @return the value of {@link ITiles#isWithPlaceholders()} of the first tile grid assuming that all tile grids use the
   *         same value
   */
  public boolean isWithPlaceholders() {
    return getTileGrids().get(0).isWithPlaceholders();
  }

  public void setTileGridLayoutConfig(TilesLayoutConfig layoutConfig) {
    getTileGrids().forEach(tileGrid -> tileGrid.setLayoutConfig(layoutConfig));
  }

  /**
   * @return the value of {@link ITiles#getLayoutConfig()} of the first tile grid assuming that all tile grids use the
   *         same value
   */
  public TilesLayoutConfig getTileGridLayoutConfig() {
    return getTileGrids().get(0).getLayoutConfig();
  }

  public class P_FilteredTilesListener implements PropertyChangeListener {

    private IGroup m_group;

    public P_FilteredTilesListener(IGroup group) {
      m_group = group;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (ITiles.PROP_FILTERED_TILES.equals(evt.getPropertyName())) {
        int numFilteredTiles = getTileGrid(m_group).getFilteredTileCount();
        m_group.setTitleSuffix("(" + numFilteredTiles + ")");
      }
      else if (ITiles.PROP_SELECTED_TILES.equals(evt.getPropertyName())) {
        List<? extends ITile> selectedTiles = getTileGrid(m_group).getSelectedTiles();
        if (selectedTiles.size() > 0) {
          // make sure only one group has selected tiles
          if (m_selectionUpdateLocked) {
            return;
          }
          m_selectionUpdateLocked = true;
          try {
            for (IGroup group : getGroupsInternal()) {
              if (group != m_group) {
                getTileGrid(group).deselectAllTiles();
              }
            }
          }
          finally {
            m_selectionUpdateLocked = false;
          }
        }
        propertySupport.setProperty(PROP_SELECTED_TILES, selectedTiles);
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
