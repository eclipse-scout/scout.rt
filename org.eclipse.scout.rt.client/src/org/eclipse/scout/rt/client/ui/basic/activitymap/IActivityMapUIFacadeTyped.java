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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;

/**
 * In Scout 3.9 IActivityMapUIFacade will be typed and this class will be removed.
 */
public interface IActivityMapUIFacadeTyped<RI, AI> {

  void setDaysFromUI(Date[] days);

  void setSelectedActivityCellFromUI(ActivityCellTyped<RI, AI> cell);

  void setSelectionFromUI(RI[] resourceIds, double[] normalizedRange);

  /**
   * Action on a empty cell or activity cell
   * 
   * @param activityCell
   *          may be null
   */
  void fireCellActionFromUI(RI resourceId, double[] normalizedRange, ActivityCellTyped<RI, AI> activityCell);

  /**
   * Popup on activity
   */
  IMenu[] fireEditActivityPopupFromUI();

  /**
   * Popup on planned activity
   */
  IMenu[] fireNewActivityPopupFromUI();

}
