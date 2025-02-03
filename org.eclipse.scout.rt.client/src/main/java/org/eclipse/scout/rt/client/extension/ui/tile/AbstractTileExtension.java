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

import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileDisposeTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileInitTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileLoadDataTileChain;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTileExtension<OWNER_FIELD extends AbstractTile> extends AbstractExtension<OWNER_FIELD>
    implements ITileExtension<OWNER_FIELD> {

  public AbstractTileExtension(OWNER_FIELD owner) {
    super(owner);
  }

  @Override
  public void execDisposeTile(TileDisposeTileChain chain) {
    chain.execDisposeTile();
  }

  @Override
  public void execInitTile(TileInitTileChain chain) {
    chain.execInitTile();
  }

  @Override
  public void execLoadData(TileLoadDataTileChain chain) {
    chain.execLoadData();
  }
}
