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
 * A tile filter is used to mask out certain tiles. All tiles that are <i>not</i> accepted by a filter, are masked out.
 * The remaining tiles (i.e. tiles that are accepted by all filters) can be retrieved by
 * {@link ITileGrid#getFilteredTiles()}. The tiles themselves are not deleted, so {@link ITileGrid#getTiles()} will
 * still return all tiles.
 */
@FunctionalInterface
public interface ITileFilter<T extends ITile> {

  /**
   * @return <code>true</code> if the given tile is accepted by the filter and should therefore be displayed, false if
   * not.
   */
  boolean accept(T tile);
}
