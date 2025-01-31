/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
