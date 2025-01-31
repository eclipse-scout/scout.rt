/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;

/**
 * All possible menu types of a tile menu. These menu types are used by {@link AbstractMenu#getConfiguredMenuTypes()}.
 */
public enum TileGridMenuType implements IMenuType {
  EmptySpace,
  SingleSelection,
  MultiSelection;

  public static void updateMenuVisibilitiesForTiles(ITileGrid<? extends ITile> tileGrid) {
    updateMenuVisibilitiesForTiles(tileGrid.getContextMenu(), tileGrid.getSelectedTiles(), null);
  }

  public static void updateMenuVisibilitiesForTiles(IContextMenu contextMenu, List<? extends ITile> selectedTiles, Predicate<IAction> filter) {
    Set<IMenuType> acceptedMenuTypes = new HashSet<>();
    acceptedMenuTypes.add(TileGridMenuType.EmptySpace);
    if (selectedTiles.size() == 1) {
      acceptedMenuTypes.add(TileGridMenuType.SingleSelection);
    }
    else if (selectedTiles.size() > 1) {
      acceptedMenuTypes.add(TileGridMenuType.MultiSelection);
    }
    updateMenuVisibilities(contextMenu, acceptedMenuTypes, filter);
  }

  /**
   * Updates the visibility of every single menu (including child menus) according to the given acceptedMenuTypes.
   *
   * @param filter
   *     (optional) menus are filtered with this predicate before visibility is updated
   */
  public static void updateMenuVisibilities(IContextMenu contextMenu, Set<IMenuType> acceptedMenuTypes, Predicate<IAction> filter) {
    final Predicate<IMenu> activeFilter = MenuUtility.createMenuFilterMenuTypes(acceptedMenuTypes, false);
    contextMenu.visit(menu -> {
      if (filter != null && !filter.test(menu)) {
        return;
      }

      if (!menu.isSeparator()) {
        menu.setVisible(activeFilter.test(menu));
      }
    }, IMenu.class);
  }
}
