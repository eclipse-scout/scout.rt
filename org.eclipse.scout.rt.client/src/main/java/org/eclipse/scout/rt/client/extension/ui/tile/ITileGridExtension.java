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

import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TileActionChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TileClickChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileGridChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITileGridExtension<T extends ITile, TG extends AbstractTileGrid<T>> extends IExtension<TG> {

  void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles);

  void execTileClick(TileClickChain<T> chain, T tile, MouseButton mouseButton);

  void execTileAction(TileActionChain<T> chain, T tile);
}
