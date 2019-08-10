/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
