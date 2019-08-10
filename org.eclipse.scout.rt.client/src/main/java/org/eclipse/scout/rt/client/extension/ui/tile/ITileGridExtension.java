/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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
