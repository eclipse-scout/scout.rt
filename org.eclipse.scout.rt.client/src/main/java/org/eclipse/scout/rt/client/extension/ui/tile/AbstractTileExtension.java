/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
