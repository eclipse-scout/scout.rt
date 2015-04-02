/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractPropertyObserverContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IActivityMapContextMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;

/**
 * The invisible root menu node of any activity map. (internal usage only)
 */
public class ActivityMapContextMenu extends AbstractPropertyObserverContextMenu<IActivityMap<?, ?>> implements IActivityMapContextMenu {

  public ActivityMapContextMenu(IActivityMap<?, ?> owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(MenuUtility.getMenuTypesForActivityMapSelection(getOwner().getSelectedActivityCell()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getOwner() != null) {
      final CompositeObject ownerValue = new CompositeObject(getOwner().getSelectedActivityCell(), getOwner().getSelectedResourceIds(), getOwner().getSelectedBeginTime(), getOwner().getSelectedEndTime());
      setCurrentMenuTypes(MenuUtility.getMenuTypesForActivityMapSelection(getOwner().getSelectedActivityCell()));
      acceptVisitor(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()));
      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (IActivityMap.PROP_SELECTED_ACTIVITY_CELL.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IActivityMap.PROP_SELECTED_RESOURCE_IDS.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IActivityMap.PROP_SELECTED_BEGIN_TIME.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IActivityMap.PROP_SELECTED_END_TIME.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }

  }

}
