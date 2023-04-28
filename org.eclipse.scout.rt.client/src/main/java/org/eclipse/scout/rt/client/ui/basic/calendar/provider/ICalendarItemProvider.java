/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.calendar.provider;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.services.common.calendar.CalendarResourceDo;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public interface ICalendarItemProvider extends IPropertyObserver {

  /**
   * type {@link Set<ICalendarItem>}
   */
  String PROP_ITEMS = "items";
  /**
   * type boolean
   */
  String PROP_MOVE_ITEM_ENABLED = "moveItemEnabled";
  /**
   * type long
   */
  String PROP_REFRESH_INTERVAL_MILLIS = "refreshIntervalMillis";
  /**
   * type boolean
   */
  String PROP_LOAD_IN_PROGRESS = "loadInProgress";
  /**
   * type CalendarResourceDo
   */
  String PROP_ASSOCIATED_RESOURCE = "associatedResource";

  void disposeProvider();

  boolean isMoveItemEnabled();

  void setMoveItemEnabled(boolean b);

  boolean isLoadInProgress();

  void setLoadInProgress(boolean b);

  long getRefreshIntervalMillis();

  void setRefreshIntervalMillis(long millis);

  CalendarResourceDo getAssociatedResource();

  void setAssociatedResource(CalendarResourceDo associatedResource);

  /**
   * @return the current set of items in the requested range
   */
  Set<ICalendarItem> getItems(Date minDate, Date maxDate);

  void reloadProvider();

  List<IMenu> getMenus();

  void onItemAction(ICalendarItem item);

  void onItemMoved(ICalendarItem item, Date fromDate, Date toDate);

}
