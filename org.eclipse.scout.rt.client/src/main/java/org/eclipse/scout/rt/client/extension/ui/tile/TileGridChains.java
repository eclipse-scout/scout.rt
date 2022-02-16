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

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TileGridChains {

  private TileGridChains() {
  }

  protected abstract static class AbstractTileGridChain<T extends ITile> extends AbstractExtensionChain<ITileGridExtension<T, ? extends AbstractTileGrid>> {

    public AbstractTileGridChain(List<? extends ITileGridExtension<T, ? extends AbstractTileGrid>> extensions) {
      super(extensions, ITileGridExtension.class);
    }
  }

  public static class TilesSelectedChain<T extends ITile> extends AbstractTileGridChain<T> {

    public TilesSelectedChain(List<? extends ITileGridExtension<T, ? extends AbstractTileGrid>> extensions) {
      super(extensions);
    }

    public void execTilesSelected(final List<T> tiles) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileGridExtension<T, ? extends AbstractTileGrid> next) {
          next.execTilesSelected(TilesSelectedChain.this, tiles);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileClickChain<T extends ITile> extends AbstractTileGridChain<T> {

    public TileClickChain(List<? extends ITileGridExtension<T, ? extends AbstractTileGrid>> extensions) {
      super(extensions);
    }

    public void execTileClick(final T tile, MouseButton mouseButton) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileGridExtension<T, ? extends AbstractTileGrid> next) {
          next.execTileClick(TileClickChain.this, tile, mouseButton);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileActionChain<T extends ITile> extends AbstractTileGridChain<T> {

    public TileActionChain(List<? extends ITileGridExtension<T, ? extends AbstractTileGrid>> extensions) {
      super(extensions);
    }

    public void execTileAction(final T tile) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileGridExtension<T, ? extends AbstractTileGrid> next) {
          next.execTileAction(TileActionChain.this, tile);
        }
      };
      callChain(methodInvocation);
    }
  }
}
