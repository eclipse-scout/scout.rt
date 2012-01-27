/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.core;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public interface ILogicalGridLayoutInfo {

  LogicalGridData[/* component count */] getGridDatas();

  Control[/* component count */] getComponents();

  int[/* component count */] getComponentWidths();

  int[/* component count */] getComponentHeights();

  int getCols(); /* number of cells horizontally */

  int getRows(); /* number of cells vertically */

  int[/* column */][/* min,pref,max */] getWidth();

  int[/* row */][/* min,pref,max */] getHeight();

  int[/*column*/] getWidthHints();

  double[/* column */] geWeightX();

  double[/* row */] getWeightY();

  Rectangle[][] layoutCellBounds(Point size);
}
