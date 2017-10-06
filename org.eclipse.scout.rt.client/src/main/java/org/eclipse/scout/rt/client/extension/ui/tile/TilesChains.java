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

import org.eclipse.scout.rt.client.ui.tile.AbstractTiles;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TilesChains {

  private TilesChains() {
  }

  protected abstract static class AbstractTilesChain extends AbstractExtensionChain<ITilesExtension<? extends AbstractTiles>> {

    public AbstractTilesChain(List<? extends ITilesExtension<? extends AbstractTiles>> extensions) {
      super(extensions, ITilesExtension.class);
    }
  }

  public static class TilesSelectedChain extends AbstractTilesChain {

    public TilesSelectedChain(List<? extends ITilesExtension<? extends AbstractTiles>> extensions) {
      super(extensions);
    }

    public void execTilesSelected(final List<? extends ITile> tiles) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITilesExtension<? extends AbstractTiles> next) {
          next.execTilesSelected(TilesSelectedChain.this, tiles);
        }
      };
      callChain(methodInvocation, tiles);
    }
  }

}
