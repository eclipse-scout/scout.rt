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
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.AbstractContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ICalendarContextMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * The invisible root menu node of any calendar. (internal usage only)
 */
@ClassId("7c6a0c17-90f1-4f1f-bad0-c6d417eaf5b5")
public class CalendarContextMenu extends AbstractContextMenu<ICalendar> implements ICalendarContextMenu {
  /**
   * @param owner
   */
  public CalendarContextMenu(ICalendar owner, List<? extends IMenu> initialChildMenus) {
    super(owner, initialChildMenus);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // set active filter
    setCurrentMenuTypes(getMenuTypesForSelection(getContainer().getSelectedComponent()));
    calculateLocalVisibility();
  }

  @Override
  public void callOwnerValueChanged() {
    handleOwnerValueChanged();
  }

  protected void handleOwnerValueChanged() {
    ICalendar container = getContainer();
    if (container != null) {
      final CalendarComponent ownerValue = container.getSelectedComponent();
      setCurrentMenuTypes(getMenuTypesForSelection(ownerValue));
      visit(new MenuOwnerChangedVisitor(ownerValue, getCurrentMenuTypes()), IMenu.class);
      calculateLocalVisibility();
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    if (ICalendar.PROP_SELECTED_COMPONENT.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
  }

  protected Set<CalendarMenuType> getMenuTypesForSelection(CalendarComponent selectedComponent) {
    if (selectedComponent == null) {
      return CollectionUtility.hashSet(CalendarMenuType.EmptySpace);
    }
    else {
      return CollectionUtility.hashSet(CalendarMenuType.CalendarComponent);
    }
  }

}
