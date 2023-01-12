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

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.TileFieldChains.TileFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;

public interface ITileFieldExtension<T extends ITileGrid<? extends ITile>, OWNER extends AbstractTileField<T>> extends IFormFieldExtension<OWNER> {

  TransferObject execDragRequest(TileFieldDragRequestChain chain);

  void execDropRequest(TileFieldDropRequestChain chain, TransferObject transferObject);
}
