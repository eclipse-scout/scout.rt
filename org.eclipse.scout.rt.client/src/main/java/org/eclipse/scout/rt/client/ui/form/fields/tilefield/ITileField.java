/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tilefield;

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;

public interface ITileField<T extends ITileGrid<? extends ITile>> extends IFormField, IDNDSupport {

  /**
   * {@link ITileGrid}
   */
  String PROP_TILE_GRID = "tileGrid";

  void setTileGrid(T tileGrid);

  T getTileGrid();

  ITileFieldUIFacade<T> getUIFacade();
}
