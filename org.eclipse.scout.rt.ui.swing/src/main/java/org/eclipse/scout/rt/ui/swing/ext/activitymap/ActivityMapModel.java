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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

/**
 * Representing a ScoutPlannerActivity
 */
public interface ActivityMapModel {

  int getRowCount();

  int getHeaderHeight();

  /**
   * @return row height in pixel
   */
  int getRowHeight(int rowIndex);

  /**
   * @return row y location in pixel
   */
  int getRowLocation(int rowIndex);

  /**
   * @return row index of y pixel coordinate
   *         <p>
   *         When y is below the lowest row then -1 is returned.
   *         <p>
   *         When y is above the highest row then rowCount is returned.
   */
  int getRowAtLocation(int y);

  ActivityComponent[] getActivities();

  double[] getActivityRange(ActivityComponent a);
}
