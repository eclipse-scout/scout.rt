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
package org.eclipse.scout.rt.client.ui.basic.calendar.provider;

import java.util.Date;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public interface ICalendarItemProvider extends IPropertyObserver {

  /**
   * type {@link ICalendarItem}[]
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

  void disposeProvider();

  void decorateCell(Cell cell, ICalendarItem item);

  boolean isMoveItemEnabled();

  void setMoveItemEnabled(boolean b);

  boolean isLoadInProgress();

  void setLoadInProgress(boolean b);

  long getRefreshIntervalMillis();

  void setRefreshIntervalMillis(long millis);

  /**
   * @return the current set of items in the requested range
   *         <p>
   *         is the currently loaded range is just a part of the requested range, a call to
   *         {@link #execItemMovedAction(ICalendarItem, Date)} is launched and will result in a
   *         PropertyChangeEvent("items")
   */
  ICalendarItem[] getItems(Date minDate, Date maxDate);

  void reloadProvider();

  IMenu[] getMenus();

  void onItemAction(ICalendarItem item) throws ProcessingException;

  void onItemMoved(ICalendarItem item, Date newDate) throws ProcessingException;

}
