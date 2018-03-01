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
      callChain(methodInvocation, tiles);
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
      callChain(methodInvocation, tile);
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
      callChain(methodInvocation, tile);
    }
  }
}
