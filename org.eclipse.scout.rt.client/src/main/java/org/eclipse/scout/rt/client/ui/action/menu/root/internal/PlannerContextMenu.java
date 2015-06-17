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
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;

/**
 * The invisible root menu node of any activity map. (internal usage only)
 */
public class PlannerContextMenu extends AbstractPropertyObserverContextMenu<IPlanner<?, ?>> implements IPlannerContextMenu {

  public PlannerContextMenu(IPlanner<?, ?> owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(MenuUtility.getMenuTypesForPlannerSelection(getOwner().getSelectedResources(), getOwner().getSelectedActivity()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getOwner() != null) {
      final CompositeObject ownerValue = new CompositeObject(getOwner().getSelectedActivity(), getOwner().getSelectedResources(), getOwner().getSelectedBeginTime(), getOwner().getSelectedEndTime());
      setCurrentMenuTypes(MenuUtility.getMenuTypesForPlannerSelection(getOwner().getSelectedResources(), getOwner().getSelectedActivity()));
      acceptVisitor(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()));
      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (IPlanner.PROP_SELECTED_ACTIVITY.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IPlanner.PROP_SELECTED_RESOURCES.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IPlanner.PROP_SELECTION_RANGE.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
  }

}
