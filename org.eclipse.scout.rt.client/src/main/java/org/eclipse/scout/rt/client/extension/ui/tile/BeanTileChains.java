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

import org.eclipse.scout.rt.client.ui.tile.AbstractBeanTile;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class BeanTileChains {

  private BeanTileChains() {
  }

  protected abstract static class AbstractBeanTileChain<BEAN> extends AbstractExtensionChain<IBeanTileExtension<BEAN, ? extends AbstractBeanTile<BEAN>>> {

    public AbstractBeanTileChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions, IBeanTileExtension.class);
    }
  }

  public static class BeanTileAppLinkActionChain<BEAN> extends AbstractBeanTileChain<BEAN> {

    public BeanTileAppLinkActionChain(List<? extends ITileExtension<? extends AbstractTile>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBeanTileExtension<BEAN, ? extends AbstractBeanTile<BEAN>> next) {
          next.execAppLinkAction(BeanTileAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation);
    }
  }
}
