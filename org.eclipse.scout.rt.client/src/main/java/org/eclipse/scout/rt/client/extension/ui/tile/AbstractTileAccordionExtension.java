/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTileAccordionExtension<T extends ITile, TILES extends AbstractTileGrid<T>> extends AbstractExtension<TILES> implements ITileGridExtension<T, TILES> {

  public AbstractTileAccordionExtension(TILES owner) {
    super(owner);
  }

  @Override
  public void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles) {
    chain.execTilesSelected(tiles);
  }
}
