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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.group.IGroup;

public abstract class AbstractTileAccordionGroupManager<T extends ITile> implements ITileAccordionGroupManager<T> {

  @Override
  public List<GroupTemplate> createGroups() {
    return Collections.emptyList();
  }

  @Override
  public GroupTemplate createGroupForTile(T tile) {
    return null;
  }

  @Override
  public Comparator<IGroup> getComparator() {
    return null;
  }

}
