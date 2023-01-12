/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;

public abstract class AbstractTileFieldExtension<T extends ITileGrid<? extends ITile>, OWNER extends AbstractTileField<T>> extends AbstractFormFieldExtension<OWNER> implements ITileFieldExtension<T, OWNER> {

  public AbstractTileFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public TransferObject execDragRequest(TileFieldDragRequestChain chain) {
    return chain.execDragRequest();
  }

  @Override
  public void execDropRequest(TileFieldDropRequestChain chain, TransferObject transferObject) {
    chain.execDropRequest(transferObject);
  }
}
