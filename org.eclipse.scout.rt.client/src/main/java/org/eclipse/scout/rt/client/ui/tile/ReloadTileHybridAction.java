/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.desktop.hybrid.AbstractHybridAction;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridActionType;
import org.eclipse.scout.rt.client.ui.tile.AbstractTile.ITileDataLoader;
import org.eclipse.scout.rt.dataobject.IDoEntity;

@HybridActionType(ReloadTileHybridAction.TYPE)
public class ReloadTileHybridAction extends AbstractHybridAction<IDoEntity> {

  protected static final String TYPE = "scout.ReloadTile";

  @Override
  public void execute(IDoEntity data) {
    try {
      AbstractTile tile = getContextElement("tile").getWidget(AbstractTile.class);
      ITileDataLoader dataLoader = tile.createDataLoader();
      if (dataLoader != null) {
        dataLoader.loadData(true);
      }
    }
    finally { // always signal the end of the action to the UI, even in the case of an error on the server
      fireHybridActionEndEvent();
    }
  }
}
