/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.group.IGroup;

public interface ITileAccordionGroupManager<T extends ITile> {

  Object getGroupIdByTile(T tile);

  /**
   * Creates a list of group templates which is used to create instances of {@link IGroup} widgets later. These groups are
   * considered static meaning they won't be deleted if they don't contain any tiles.
   * <p>
   * Use this method if you know which groups exist and want to show them even if they don't contain any tiles. If you
   * need dynamic groups, you can use {@link #createGroupForTile(ISimpleTile)}.
   */
  List<GroupTemplate> createGroups();

  /**
   * Creates a group template for a given tile. This group is a dynamic group meaning it will be created if a tile is
   * added and may not assigned to an existing group. And it will be deleted if all tiles are deleted from that group. If
   * you need static groups, use {@link #createGroups()} instead.
   * <p>
   * This method is called if a tile may not be assigned to a group. Return null if the tile should be assigned to the
   * default group ({@link DefaultGroupManager}).
   *
   * @return a {@link GroupTemplate} or null to assign it to the default group.
   */
  GroupTemplate createGroupForTile(T tile);

  /**
   * An ID used to store and retrieve a group handler from a HashMap. You may use a string, the class handler class itself
   * or any other ID that suits your needs.
   */
  Object getId();

  /**
   * The comparator to sort the groups. If null is returned, the comparator of the accordion is used.
   */
  Comparator<? extends IGroup> getComparator();
}
