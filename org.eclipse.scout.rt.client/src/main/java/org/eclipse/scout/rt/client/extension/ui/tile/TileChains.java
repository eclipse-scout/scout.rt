/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TileChains {

  private TileChains() {
  }

  public abstract static class AbstractTileChain extends AbstractExtensionChain<ITileExtension<? extends AbstractTile>> {

    public AbstractTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions, ITileExtension.class);
    }

  }

  public static class TileDisposeTileChain extends AbstractTileChain {

    public TileDisposeTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions);
    }

    public void execDisposeTile() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileExtension<? extends AbstractTile> next) {
          next.execDisposeTile(TileDisposeTileChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileInitTileChain extends AbstractTileChain {

    public TileInitTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions);
    }

    public void execInitTile() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileExtension<? extends AbstractTile> next) {
          next.execInitTile(TileInitTileChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileLoadDataTileChain extends AbstractTileChain {

    public TileLoadDataTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions);
    }

    public void execLoadData() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileExtension<? extends AbstractTile> next) {
          next.execLoadData(TileLoadDataTileChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
