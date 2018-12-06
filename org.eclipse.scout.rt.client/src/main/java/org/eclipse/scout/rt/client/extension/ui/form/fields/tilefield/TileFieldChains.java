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
package org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TileFieldChains {

  private TileFieldChains() {
  }

  protected abstract static class AbstractTileFieldChain<T extends ITileGrid<? extends ITile>> extends AbstractExtensionChain<ITileFieldExtension<T, ? extends AbstractTileField>> {

    public AbstractTileFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITileFieldExtension.class);
    }
  }

  public static class TileFieldDragRequestChain<T extends ITileGrid<? extends ITile>> extends AbstractTileFieldChain<T> {

    public TileFieldDragRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public TransferObject execDragRequest() {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITileFieldExtension<T, ? extends AbstractTileField> next) {
          setReturnValue(next.execDragRequest(TileFieldDragRequestChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class TileFieldDropRequestChain<T extends ITileGrid<? extends ITile>> extends AbstractTileFieldChain<T> {

    public TileFieldDropRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDropRequest(final TransferObject transferObject) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITileFieldExtension<T, ? extends AbstractTileField> next) {
          next.execDropRequest(TileFieldDropRequestChain.this, transferObject);
        }
      };
      callChain(methodInvocation, transferObject);
    }
  }
}
