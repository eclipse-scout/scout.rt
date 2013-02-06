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

public class MultiSplitLayoutConstraints {

  public int row;
  public int col;
  public int tabIndex;
  public float[][] distributionMap;

  public MultiSplitLayoutConstraints(int row, int col, int tabIndex, float[][] distributionMap) {
    if (distributionMap == null || distributionMap.length != 3 || distributionMap[0].length != 3) {
      throw new IllegalArgumentException("distribution map must be a 3x3 matrix (row,col)");
    }
    this.row = row;
    this.col = col;
    this.tabIndex = tabIndex;
    this.distributionMap = new float[3][3];
    System.arraycopy(distributionMap[0], 0, this.distributionMap[0], 0, 3);
    System.arraycopy(distributionMap[1], 0, this.distributionMap[1], 0, 3);
    System.arraycopy(distributionMap[2], 0, this.distributionMap[2], 0, 3);
  }

  public MultiSplitLayoutConstraints(int row, int col, int tabIndex, float[] distributionMap) {
    if (distributionMap == null || distributionMap.length != 9) {
      throw new IllegalArgumentException("distribution map must be a vector containing 9 elements (3 rows X 3 columns)");
    }
    this.row = row;
    this.col = col;
    this.tabIndex = tabIndex;
    this.distributionMap = new float[3][3];
    System.arraycopy(distributionMap, 0, this.distributionMap[0], 0, 3);
    System.arraycopy(distributionMap, 3, this.distributionMap[1], 0, 3);
    System.arraycopy(distributionMap, 6, this.distributionMap[2], 0, 3);
  }
}
