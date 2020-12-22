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
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.TileGridMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITileGridContextMenu;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The invisible root menu node of tile grid. (internal usage only)
 */
@ClassId("6c1c8e1a-bee2-49fc-8bcc-e2169037fb7e")
public class TileGridContextMenu extends AbstractContextMenu<ITileGrid<? extends ITile>> implements ITileGridContextMenu {

  public TileGridContextMenu(ITileGrid<? extends ITile> owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(getMenuTypesForSelection(getContainer().getSelectedTiles()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    ITileGrid<? extends ITile> container = getContainer();
    if (container == null) {
      return;
    }

    final List<? extends ITile> ownerValue = container.getSelectedTiles();
    setCurrentMenuTypes(getMenuTypesForSelection(ownerValue));
    visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
    calculateLocalVisibility();
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (ITileGrid.PROP_SELECTED_TILES.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    // FIXME [8.0] CGU tiles necessary to handle tile update events (e.g. enabled) as done in table?
    // either add a method to the grid that allows to specify widget-listeners which are then applied to all tiles in the gird (and removed if tiles are removed)
    // this is flexible but maybe has bad performance because lot of events may be fired for grids having lots of tiles.
    // another solution would be to add dedicated tile-grid events which allows the grid to buffer events (setChanging, etc.) for batch updates.
  }

  @Override
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
  }

  protected Set<TileGridMenuType> getMenuTypesForSelection(List<? extends ITile> selection) {
    if (CollectionUtility.isEmpty(selection)) {
      return CollectionUtility.hashSet(TileGridMenuType.EmptySpace);
    }
    if (CollectionUtility.size(selection) == 1) {
      return CollectionUtility.hashSet(TileGridMenuType.SingleSelection);
    }
    return CollectionUtility.hashSet(TileGridMenuType.MultiSelection);
  }
}
