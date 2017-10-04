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
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.tile.TileChains.TileDisposeTileChain;
import org.eclipse.scout.rt.client.ui.tile.TileChains.TileInitTileChain;
import org.eclipse.scout.rt.client.ui.tile.TileChains.TileLoadDataTileChain;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITileExtension<OWNER extends AbstractTile> extends IExtension<OWNER> {

  void execInitTile(TileInitTileChain chain);

  void execDisposeTile(TileDisposeTileChain chain);

  void execLoadData(TileLoadDataTileChain chain);
}
