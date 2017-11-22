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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.TEXTS;

@ClassId("e1e96659-f922-45c8-b350-78f9de059a83")
public abstract class AbstractTilesAccordion extends AbstractAccordion {

  public static final String PROP_SHOW_FILTER_COUNT = "showFilterCount";
  public static final String PROP_SELECTED_TILES = "selectedTiles";

  private List<ITileFilter> m_tileFilters;
  private Map<Object, ITilesAccordionGroupManager> m_groupManagers;
  private ITilesAccordionGroupManager m_groupManager;
  private boolean m_selectionUpdateLocked = false;

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
    setGroupManager(new DefaultGroupManager());
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

  public void addTile(ITile tile) {
    getTileGrid(getGroupByTile(tile)).addTile(tile);
    updateGroupStates();
  }

  protected IGroup getGroupByTile(ITile tile) {
    Object groupId = m_groupManager.getGroupIdByTile(tile);
    return getGroupById(groupId);
  }

  protected IGroup getGroupById(Object groupId) {
    return getGroups().stream()
        .filter(group -> ObjectUtility.equals(groupId, group.getGroupId()))
        .findFirst()
        .orElse(getDefaultGroup());
  }

  /**
   * The default group is always the last group.
   */
  @SuppressWarnings("unchecked")
  protected <T extends IGroup> T getDefaultGroup() {
    List<? extends IGroup> groups = getGroupsInternal();
    int lastGroup = groups.size() - 1;
    return (T) groups.get(lastGroup);
  }

  public void setTiles(List<ITile> tiles) {
    Map<IGroup, List<ITile>> map = new HashMap<>();
    for (IGroup group : getGroupsInternal()) {
      map.put(group, new ArrayList<>());
    }
    for (ITile tile : tiles) {
      map.get(getGroupByTile(tile)).add(tile);
    }
    for (Entry<IGroup, List<ITile>> entry : map.entrySet()) {
      getTileGrid(entry.getKey()).setTiles(entry.getValue());
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

  protected ITiles getTileGrid(IGroup group) {
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
  public void setGroupManager(ITilesAccordionGroupManager groupManager) {
    addGroupManager(groupManager);
    activateGroupManager(groupManager.getId());
  }

  public void addGroupManager(ITilesAccordionGroupManager groupManager) {
    m_groupManagers.put(groupManager.getId(), groupManager);
  }

  public void removeGroupManager(ITilesAccordionGroupManager groupManager) {
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
    ITilesAccordionGroupManager groupManager = m_groupManagers.get(groupManagerId);
    if (groupManager == null) {
      throw new IllegalArgumentException("No group manager registered for ID " + groupManagerId);
    }
    if (groupManager == m_groupManager) {
      // manager already active, do nothing
      return;
    }
    m_groupManager = groupManager;

    List<ITile> allTiles = getAllTiles();
    List<? extends IGroup> currentGroups = getGroupsInternal();

    // We always add a default group which is used when the DefaultGroupManager is active or
    // when a group manager is active and one or more tile could not be put into a matching group
    // thus this group acts as a "catch-all" for tiles without a group.
    GroupTemplate defaultGroup = new GroupTemplate(DefaultGroupManager.GROUP_ID_DEFAULT, TEXTS.get("NotGrouped"));
    List<GroupTemplate> requiredGroups = new ArrayList<GroupTemplate>();
    requiredGroups.addAll(m_groupManager.createGroups());
    requiredGroups.add(defaultGroup);
    int currentSize = currentGroups.size();
    int requiredSize = requiredGroups.size();

    // delete all groups we don't need anymore
    if (currentSize > requiredSize) {
      for (int i = requiredSize; i < currentSize; i++) {
        deleteGroup(currentGroups.get(i));
      }
    }

    // add the missing groups
    if (currentSize < requiredSize) {
      for (int i = currentSize; i < requiredSize; i++) {
        addGroup(createGroup());
      }
    }

    // Make sure that the all groups have the properties set as defined by the requiredGroups
    // Note: since we re-use existing groups we might throw away some groups returned by createGroups
    currentGroups = getGroupsInternal();
    for (int i = 0; i < requiredSize; i++) {
      IGroup group = currentGroups.get(i);
      GroupTemplate groupTemplate = requiredGroups.get(i);
      group.setTitle(groupTemplate.getTitle());
      group.setGroupId(groupTemplate.getGroupId());
      group.setCssClass(groupTemplate.getCssClass());
      group.setCollapsed(groupTemplate.isCollapsed());
      group.setHeaderVisible(groupTemplate.isHeaderVisible());
    }

    setTiles(allTiles);

    for (ITileFilter filter : m_tileFilters) {
      addFilterToAllTileGrids(filter);
    }
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

  public Stream<ITiles> streamAllTileGrids() {
    return getAllTileGrids().stream();
  }

  public List<ITiles> getAllTileGrids() {
    List<ITiles> tileGrids = new ArrayList<>();
    for (IGroup group : getGroups()) {
      tileGrids.add((ITiles) group.getBody());
    }
    return tileGrids;
  }

  public Stream<ITile> streamAllTiles() {
    return getAllTiles().stream();
  }

  public List<ITile> getAllTiles() {
    List<ITile> allTiles = new ArrayList<>();
    for (ITiles tiles : getAllTileGrids()) {
      allTiles.addAll(tiles.getTiles());
    }
    return allTiles;
  }

  public void addTilesFilter(ITileFilter filter) {
    m_tileFilters.add(filter);
    addFilterToAllTileGrids(filter);
  }

  protected void addFilterToAllTileGrids(ITileFilter filter) {
    streamAllTileGrids().forEach(tileGrid -> tileGrid.addFilter(filter));
  }

  public void removeTilesFilter(ITileFilter filter) {
    m_tileFilters.remove(filter);
    removeFilterFromAllTileGrids(filter);
  }

  protected void removeFilterFromAllTileGrids(ITileFilter filter) {
    streamAllTileGrids().forEach(tileGrid -> tileGrid.removeFilter(filter));
  }

  public void deleteAllTiles() {
    streamAllTileGrids().forEach(ITiles::deleteAllTiles);
  }

  public void deleteTile(ITile tile) {
    streamAllTileGrids().forEach(tileGrid -> tileGrid.deleteTile(tile));
  }

  public void filterTiles() {
    streamAllTileGrids().forEach(ITiles::filter);
  }

  public void sortTiles(Comparator<ITile> comparator) {
    streamAllTileGrids().forEach(tileGrid -> {
      List<? extends ITile> sortedTiles = new ArrayList<>(tileGrid.getTiles());
      Collections.sort(sortedTiles, comparator);
      tileGrid.setTiles(sortedTiles);
    });

  }

  public ITile getSelectedTile() {
    for (ITiles tileGrid : getAllTileGrids()) {
      if (tileGrid.getSelectedTile() != null) {
        return tileGrid.getSelectedTile();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<ITile> getSelectedTiles() {
    for (ITiles tileGrid : getAllTileGrids()) {
      List<? extends ITile> selectedTiles = tileGrid.getSelectedTiles();
      if (selectedTiles != null && selectedTiles.size() != 0) {
        return (List<ITile>) selectedTiles;
      }
    }
    return Collections.emptyList();
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

}
