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

/**
 * Default group handler has only a single group. The header of that group is invisible.
 */
public class DefaultGroupManager<T extends ITile> extends AbstractTileAccordionGroupManager<T> {

  public static final Object ID = DefaultGroupManager.class;

  public static final Object GROUP_ID_DEFAULT = "default";

  @Override
  public Object getGroupIdByTile(ITile tile) {
    return GROUP_ID_DEFAULT;
  }

  @Override
  public Object getId() {
    return ID;
  }
}
