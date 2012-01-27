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
package org.eclipse.scout.rt.ui.swing.window.desktop.layout;

/**
 * This model can be passed to a {@link MultiSplitLayout} of a desktop to customize splitter handling
 * The two splits on left and right (top/bottom) edge are assumed to be the fixed 0 and WIDTH splits.
 */
public interface IMultiSplitStrategy {

  /**
   * Called by {@link MultiSplitLayout} when the span of the multisplit area changed (width or height)
   */
  void updateSpan(int newSpan);

  /**
   * @param splitIndex
   * @return the location of the split
   */
  int getSplitLocation(int row, int col);

  /**
   * @param splitIndex
   * @param newLocation
   *          the new location of the split
   */
  void setSplitLocation(int row, int col, int newLocation);

}
