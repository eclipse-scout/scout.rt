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

import java.util.List;

import org.eclipse.scout.rt.client.ui.group.IGroup;

public interface ITilesAccordionGroupManager {

  Object getGroupIdByTile(ITile tile);

  /**
   * Creates a list of group templates which is used to create instances of {@link IGroup} widgets later. All tiles are
   * passed to this method, so a group manager can create groups based on the given tiles. However a group manager may
   * also create groups without looking at the tiles at all.
   */
  List<GroupTemplate> createGroups(List<ITile> allTiles);

  /**
   * An ID used to store and retrieve a group handler from a HashMap. You may use a string, the class handler class
   * itself or any other ID that suits your needs.
   */
  Object getId();

}
