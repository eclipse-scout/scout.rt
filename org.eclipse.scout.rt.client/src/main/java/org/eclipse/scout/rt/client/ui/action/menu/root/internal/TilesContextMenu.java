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
package org.eclipse.scout.rt.client.ui.action.menu.root.internal;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITilesContextMenu;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITiles;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * The invisible root menu node of tiles. (internal usage only)
 */
@ClassId("6c1c8e1a-bee2-49fc-8bcc-e2169037fb7e")
public class TilesContextMenu extends AbstractContextMenu<ITiles> implements ITilesContextMenu {

  /**
   * @param owner
   */
  public TilesContextMenu(ITiles owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(MenuUtility.getMenuTypesForTilesSelection(getContainer().getSelectedTiles()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getContainer() != null) {
      final List<? extends ITile> ownerValue = getContainer().getSelectedTiles();
      setCurrentMenuTypes(MenuUtility.getMenuTypesForTilesSelection(ownerValue));
      acceptVisitor(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()));
      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (evt.getPropertyName() == ITiles.PROP_SELECTED_TILES) {
      handleOwnerValueChanged();
    }
    // FIXME [7.1] CGU tiles necessary to handle tile update events as done in table?
  }

}
