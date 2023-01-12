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

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileAccordion;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TileAccordionChains {

  private TileAccordionChains() {
  }

  protected abstract static class AbstractTileAccordionChain<T extends ITile> extends AbstractExtensionChain<ITileAccordionExtension<T, ? extends AbstractTileAccordion>> {

    public AbstractTileAccordionChain(List<? extends ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions) {
      super(extensions, ITileAccordionExtension.class);
    }
  }

  public static class TilesSelectedChain<T extends ITile> extends AbstractTileAccordionChain<T> {

    public TilesSelectedChain(List<? extends ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions) {
      super(extensions);
    }

    public void execTilesSelected(final List<T> tiles) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileAccordionExtension<T, ? extends AbstractTileAccordion> next) {
          next.execTilesSelected(TilesSelectedChain.this, tiles);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileClickChain<T extends ITile> extends AbstractTileAccordionChain<T> {

    public TileClickChain(List<? extends ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions) {
      super(extensions);
    }

    public void execTileClick(final T tile, MouseButton mouseButton) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileAccordionExtension<T, ? extends AbstractTileAccordion> next) {
          next.execTileClick(TileClickChain.this, tile, mouseButton);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TileActionChain<T extends ITile> extends AbstractTileAccordionChain<T> {

    public TileActionChain(List<? extends ITileAccordionExtension<T, ? extends AbstractTileAccordion>> extensions) {
      super(extensions);
    }

    public void execTileAction(final T tile) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileAccordionExtension<T, ? extends AbstractTileAccordion> next) {
          next.execTileAction(TileActionChain.this, tile);
        }
      };
      callChain(methodInvocation);
    }
  }
}
