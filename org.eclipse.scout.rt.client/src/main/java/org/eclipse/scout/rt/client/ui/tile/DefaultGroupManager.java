/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
