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

import org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent;
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

  public static class TileDataChangedTileChain extends AbstractTileChain {

    public TileDataChangedTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions);
    }

    public void execDataChanged(DataChangeEvent event) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileExtension<? extends AbstractTile> next) {
          next.execDataChanged(TileDataChangedTileChain.this, event);
        }
      };
      callChain(methodInvocation);
    }
  }
}
