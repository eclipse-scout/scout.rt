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
package org.eclipse.scout.rt.client.extension.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TileActionChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TileClickChain;
import org.eclipse.scout.rt.client.extension.ui.tile.TileAccordionChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileAccordion;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface ITileAccordionExtension<T extends ITile, A extends AbstractTileAccordion<T>> extends IExtension<A> {

  void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles);

  void execTileClick(TileClickChain<T> chain, T tile, MouseButton mouseButton);

  void execTileAction(TileActionChain<T> chain, T tile);

}
