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

import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileDataChangedTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileDisposeTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileInitTileChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileChains.TileLoadDataTileChain;
import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITileExtension<OWNER extends AbstractTile> extends IExtension<OWNER> {

  void execInitTile(TileInitTileChain chain);

  void execDisposeTile(TileDisposeTileChain chain);

  void execLoadData(TileLoadDataTileChain chain);

  void execDataChanged(TileDataChangedTileChain chain, DataChangeEvent event);
}
