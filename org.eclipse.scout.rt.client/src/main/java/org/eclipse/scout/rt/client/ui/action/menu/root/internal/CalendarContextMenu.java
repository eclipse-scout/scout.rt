/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

  public CalendarContextMenu(ICalendar container, List<? extends IMenu> initialChildMenus) {
    super(container, initialChildMenus);
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
  protected boolean isOwnerPropertyChangedListenerRequired() {
    return true;
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
    return CollectionUtility.hashSet(CalendarMenuType.CalendarComponent);
  }
}
