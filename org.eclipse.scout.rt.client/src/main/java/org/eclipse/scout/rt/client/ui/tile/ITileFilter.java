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

/**
 * A tile filter is used to mask out certain tiles. All tiles that are <i>not</i> accepted by a filter, are masked out.
 * The remaining tiles (i.e. tiles that are accepted by all filters) can be retrieved by
 * {@link ITiles#getFilteredTiles()}. The tiles themselves are not deleted, so {@link ITiles#getTiles()} will still
 * return all tiles.
 */
@FunctionalInterface
public interface ITileFilter {

  /**
   * @return <code>true</code> if the given tile is accepted by the filter and should therefore be displayed, false if
   *         not.
   */
  boolean accept(ITile tile);
}
