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

public interface IActivityMapUIFacade {

  void setDaysFromUI(Date[] days);

  void setSelectedActivityCellFromUI(ActivityCell cell);

  void setSelectionFromUI(Long[] resourceIds, double[] normalizedRange);

  /**
   * Action on a empty cell or activity cell
   * 
   * @param activityCell
   *          may be null
   */
  void fireCellActionFromUI(long resourceId, double[] normalizedRange, ActivityCell activityCell);

  /**
   * Popup on activity
   */
  IMenu[] fireEditActivityPopupFromUI();

  /**
   * Popup on planned activity
   */
  IMenu[] fireNewActivityPopupFromUI();

}
