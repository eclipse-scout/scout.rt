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
package org.eclipse.scout.rt.client.extension.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTileGridExtension<T extends ITile, TILES extends AbstractTileGrid<T>> extends AbstractExtension<TILES> implements ITileGridExtension<T, TILES> {

  public AbstractTileGridExtension(TILES owner) {
    super(owner);
  }

  @Override
  public void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles) {
    chain.execTilesSelected(tiles);
  }

}
