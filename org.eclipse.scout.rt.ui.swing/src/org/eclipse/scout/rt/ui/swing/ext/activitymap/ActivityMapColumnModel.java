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

public interface ActivityMapColumnModel {

  int SMALL = 0;
  int MEDIUM = 1;
  int LARGE = 2;

  Object[] getMajorColumns();

  Object[] getMinorColumns(Object majorColumn);

  /**
   * normalized range [begin,end] in the range [0..1,0..1]
   */
  double[] getMajorColumnRange(Object majorColumn);

  /**
   * normalized range [begin,end] in the range [0..1,0..1]
   */
  double[] getMinorColumnRange(Object minorColumn);

  /**
   * normalized snap to minor column
   */
  double[] snapRange(double d);

  /**
   * @param size
   *          SMALL, MEDIUM, LARGE
   */
  String getMajorColumnText(Object column, int size);

  /**
   * @param size
   *          SMALL, MEDIUM, LARGE
   */
  String getMinorColumnText(Object column, int size);

  String getMajorColumnTooltipText(Object column);

  String getMinorColumnTooltipText(Object column);
}
