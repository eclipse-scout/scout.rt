/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.PlannerMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerAdapter;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.Range;

/**
 * The invisible root menu node of any activity map. (internal usage only)
 */
@ClassId("4e3da25c-da4c-4258-aa24-e057aa9004d7")
public class PlannerContextMenu extends AbstractContextMenu<IPlanner<?, ?>> implements IPlannerContextMenu {

  public PlannerContextMenu(IPlanner<?, ?> owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getContainer().addPlannerListener(new P_OwnerPlannerListener());
    // set active filter
    setCurrentMenuTypes(getMenuTypesForSelection(getContainer().getSelectedResources(), getContainer().getSelectedActivity(), getContainer().getSelectionRange()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    IPlanner<?, ?> container = getContainer();
    if (container != null) {
      final CompositeObject ownerValue = new CompositeObject(container.getSelectedResources(), container.getSelectedActivity(), container.getSelectionRange());
      setCurrentMenuTypes(getMenuTypesForSelection(container.getSelectedResources(), container.getSelectedActivity(), container.getSelectionRange()));
      visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
      calculateLocalVisibility();
    }
  }

  @Override
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (IPlanner.PROP_SELECTED_ACTIVITY.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
    else if (IPlanner.PROP_SELECTION_RANGE.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
  }

  protected Set<PlannerMenuType> getMenuTypesForSelection(List<? extends Resource<?>> selectedResources, Activity<?, ?> selectedActivity, Range<Date> selectionRange) {
    if (CollectionUtility.isEmpty(selectedResources)) {
      return CollectionUtility.hashSet(PlannerMenuType.EmptySpace);
    }
    Set<PlannerMenuType> menuTypes = new HashSet<>();
    if (CollectionUtility.size(selectedResources) > 0) {
      menuTypes.add(PlannerMenuType.Resource);
    }
    if (selectedActivity != null) {
      menuTypes.add(PlannerMenuType.Activity);
    }
    else if (selectionRange.getFrom() != null || selectionRange.getTo() != null) {
      menuTypes.add(PlannerMenuType.Range);
    }
    return menuTypes;
  }

  private class P_OwnerPlannerListener extends PlannerAdapter {
    @Override
    public void plannerChanged(PlannerEvent e) {
      if (e.getType() == PlannerEvent.TYPE_RESOURCES_SELECTED) {
        handleOwnerValueChanged();
      }
    }
  }
}
