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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.TreeSet;

class LogicalGridLayoutInfo {
  LogicalGridData[/* component count */] gridDatas;
  Component[/* component count */] components;
  int cols;/* number of cells horizontally */
  int rows;/* number of cells vertically */
  int[/* column */][/* min,pref,max */] width;
  int[/* row */][/* min,pref,max */] height;
  double[/* column */] weightX;
  double[/* row */] weightY;
  private int m_hgap;
  private int m_vgap;

  LogicalGridLayoutInfo(ISwingEnvironment env, Component[] components, LogicalGridData[] cons, int hgap, int vgap) {
    this.components = components;
    m_hgap = hgap;
    m_vgap = vgap;
    // create a modifiable copy of the grid datas
    this.gridDatas = new LogicalGridData[cons.length];
    for (int i = 0; i < cons.length; i++) {
      this.gridDatas[i] = new LogicalGridData(cons[i]);
    }
    if (components.length == 0) {
      this.cols = 0;
      this.rows = 0;
      this.width = new int[0][0];
      this.height = new int[0][0];
      this.weightX = new double[0];
      this.weightY = new double[0];
      return;
    }
    // eliminate unused rows and columns
    TreeSet<Integer> usedCols = new TreeSet<Integer>();
    TreeSet<Integer> usedRows = new TreeSet<Integer>();
    // ticket 86645 use member gridDatas instead of param cons
    for (LogicalGridData gd : gridDatas) {
      if (gd.gridx < 0) gd.gridx = 0;
      if (gd.gridy < 0) gd.gridy = 0;
      if (gd.gridw < 1) gd.gridw = 1;
      if (gd.gridh < 1) gd.gridh = 1;
      for (int x = gd.gridx; x < gd.gridx + gd.gridw; x++) {
        usedCols.add(x);
      }
      for (int y = gd.gridy; y < gd.gridy + gd.gridh; y++) {
        usedRows.add(y);
      }
    }
    int maxCol = usedCols.last();
    for (int x = maxCol; x >= 0; x--) {
      if (!usedCols.contains(x)) {
        // eliminate column
        // ticket 86645 use member gridDatas instead of param cons
        for (LogicalGridData gd : gridDatas) {
          if (gd.gridx > x) {
            gd.gridx--;
          }
        }
      }
    }
    int maxRow = usedRows.last();
    for (int y = maxRow; y >= 0; y--) {
      if (!usedRows.contains(y)) {
        // eliminate row
        // ticket 86645 use member gridDatas instead of param cons
        for (LogicalGridData gd : gridDatas) {
          if (gd.gridy > y) {
            // ticket 86645
            gd.gridy--;
          }
        }
      }
    }
    //
    this.cols = usedCols.size();
    this.rows = usedRows.size();
    this.width = new int[cols][3];
    this.height = new int[rows][3];
    this.weightX = new double[cols];
    this.weightY = new double[rows];
    initializeInfo(env, hgap, vgap);
  }

  private void initializeInfo(ISwingEnvironment env, int hgap, int vgap) {
    int compCount = components.length;
    Dimension[] compSize = new Dimension[compCount];
    // cache component sizes and cleanup constraints
    for (int i = 0; i < compCount; i++) {
      Component comp = components[i];
      LogicalGridData cons = gridDatas[i];
      Dimension d = uiSizeInPixel(comp);
      if (cons.widthHint > 0) {
        d.width = cons.widthHint;
      }
      if (cons.heightHint > 0) {
        d.height = cons.heightHint;
      }
      compSize[i] = d;
      if (cons.gridx < 0) cons.gridx = 0;
      if (cons.gridy < 0) cons.gridy = 0;
      if (cons.gridw < 1) cons.gridw = 1;
      if (cons.gridh < 1) cons.gridh = 1;
      if (cons.gridx >= cols) cons.gridx = cols - 1;
      if (cons.gridy >= rows) cons.gridy = rows - 1;
      if (cons.gridx + cons.gridw - 1 >= cols) cons.gridw = cols - cons.gridx;
      if (cons.gridy + cons.gridh >= rows) cons.gridh = rows - cons.gridy;
    }
    initializeColumns(env, compSize, hgap);
    initializeRows(env, compSize, vgap);
  }

  private void initializeColumns(ISwingEnvironment env, Dimension[] compSize, int hgap) {
    int compCount = compSize.length;
    int[] prefWidths = new int[cols];
    boolean[] fixedWidths = new boolean[cols];
    for (int i = 0; i < compCount; i++) {
      LogicalGridData cons = gridDatas[i];
      if (cons.gridw == 1) {
        int prefw;
        if (cons.widthHint > 0) {
          prefw = cons.widthHint;
        }
        else if (cons.useUiWidth) {
          prefw = compSize[i].width;
        }
        else {
          prefw = logicalWidthInPixel(env, cons);
        }
        for (int j = cons.gridx; j < cons.gridx + cons.gridw && j < cols; j++) {
          prefWidths[j] = Math.max(prefWidths[j], prefw);
          if (cons.weightx == 0) {
            fixedWidths[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < compCount; i++) {
      LogicalGridData cons = gridDatas[i];
      if (cons.gridw > 1) {
        int hSpan = cons.gridw;
        int spanWidth;
        int distWidth;
        // pref
        spanWidth = 0;
        for (int j = cons.gridx; j < cons.gridx + cons.gridw && j < cols; j++) {
          if (!fixedWidths[j]) {
            spanWidth += prefWidths[j];
          }
        }
        if (cons.widthHint > 0) {
          distWidth = cons.widthHint - spanWidth - (hSpan - 1) * hgap;
        }
        else if (cons.useUiWidth) {
          distWidth = compSize[i].width - spanWidth - (hSpan - 1) * hgap;
        }
        else {
          distWidth = logicalWidthInPixel(env, cons) - spanWidth - (hSpan - 1) * hgap;
        }
        if (distWidth > 0) {
          int equalWidth = (distWidth + spanWidth) / hSpan;
          int remainder = (distWidth + spanWidth) % hSpan;
          int last = -1;
          for (int j = cons.gridx; j < cons.gridx + cons.gridw && j < cols; j++) {
            if (fixedWidths[j]) {
              prefWidths[last = j] = prefWidths[j];
            }
            else {
              prefWidths[last = j] = Math.max(equalWidth, prefWidths[j]);
            }
            if (cons.weightx == 0) {
              fixedWidths[j] = true;
            }
          }
          if (last > -1) {
            prefWidths[last] += remainder;
          }
        }
      }
    }
    for (int i = 0; i < cols; i++) {
      if (fixedWidths[i]) {
        width[i][SwingLayoutUtility.MIN] = prefWidths[i];
        width[i][SwingLayoutUtility.PREF] = prefWidths[i];
        width[i][SwingLayoutUtility.MAX] = prefWidths[i];
      }
      else {
        width[i][SwingLayoutUtility.MIN] = 0;// must be exactly 0!
        width[i][SwingLayoutUtility.PREF] = prefWidths[i];
        width[i][SwingLayoutUtility.MAX] = 10240;
      }
    }
    // averaged column weights, normalized so that sum of weights is equal to
    // 1.0
    for (int i = 0; i < cols; i++) {
      if (fixedWidths[i]) {
        weightX[i] = 0;
      }
      else {
        double weightSum = 0;
        int weightCount = 0;
        for (int k = 0; k < compCount; k++) {
          LogicalGridData cons = gridDatas[k];
          if (cons.weightx > 0 && cons.gridx <= i && i <= cons.gridx + cons.gridw - 1) {
            weightSum += (cons.weightx / cons.gridw);
            weightCount++;
          }
        }
        weightX[i] = (weightCount > 0 ? weightSum / weightCount : 0);
      }
    }
    double sumWeightX = 0;
    for (int i = 0; i < cols; i++) {
      sumWeightX += weightX[i];
    }
    if (sumWeightX >= 1e-6) {
      double f = 1.0 / sumWeightX;
      for (int i = 0; i < cols; i++) {
        weightX[i] = weightX[i] * f;
      }
    }
  }

  private void initializeRows(ISwingEnvironment env, Dimension[] compSize, int vgap) {
    int compCount = compSize.length;
    int[] prefHeights = new int[rows];
    boolean[] fixedHeights = new boolean[rows];
    for (int i = 0; i < compCount; i++) {
      LogicalGridData cons = gridDatas[i];
      if (cons.gridh == 1) {
        int prefh;
        if (cons.heightHint > 0) {
          prefh = cons.heightHint;
        }
        else if (cons.useUiHeight) {
          prefh = compSize[i].height;
        }
        else {
          prefh = logicalHeightInPixel(env, cons);
        }
        for (int j = cons.gridy; j < cons.gridy + cons.gridh && j < rows; j++) {
          prefHeights[j] = Math.max(prefHeights[j], prefh);
          if (cons.weighty == 0) {
            fixedHeights[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < compCount; i++) {
      LogicalGridData cons = gridDatas[i];
      if (cons.gridh > 1) {
        int vspan = cons.gridh;
        int spanHeight;
        int distHeight;
        // pref
        spanHeight = 0;
        for (int j = cons.gridy; j < cons.gridy + cons.gridh && j < rows; j++) {
          spanHeight += prefHeights[j];
        }
        if (cons.heightHint > 0) {
          distHeight = cons.heightHint - spanHeight - (vspan - 1) * vgap;
        }
        else if (cons.useUiHeight) {
          distHeight = compSize[i].height - spanHeight - (vspan - 1) * vgap;
        }
        else {
          distHeight = logicalHeightInPixel(env, cons) - spanHeight - (vspan - 1) * vgap;
        }
        if (distHeight > 0) {
          int equalHeight = (distHeight + spanHeight) / vspan;
          int remainder = (distHeight + spanHeight) % vspan;
          int last = -1;
          for (int j = cons.gridy; j < cons.gridy + cons.gridh && j < rows; j++) {
            prefHeights[last = j] = Math.max(equalHeight, prefHeights[j]);
            if (cons.weighty == 0) {
              fixedHeights[j] = true;
            }
          }
          if (last > -1) {
            prefHeights[last] += remainder;
          }
        }
      }
    }
    for (int i = 0; i < rows; i++) {
      if (fixedHeights[i]) {
        height[i][SwingLayoutUtility.MIN] = prefHeights[i];
        height[i][SwingLayoutUtility.PREF] = prefHeights[i];
        height[i][SwingLayoutUtility.MAX] = prefHeights[i];
      }
      else {
        height[i][SwingLayoutUtility.MIN] = 0;// must be exactly 0!
        height[i][SwingLayoutUtility.PREF] = prefHeights[i];
        height[i][SwingLayoutUtility.MAX] = 10240;
      }
    }
    // averaged row weights, normalized so that sum of weights is equal to 1.0
    for (int i = 0; i < rows; i++) {
      if (fixedHeights[i]) {
        weightY[i] = 0;
      }
      else {
        double weightSum = 0;
        int weightCount = 0;
        for (int k = 0; k < compCount; k++) {
          LogicalGridData cons = gridDatas[k];
          if (cons.weighty > 0 && cons.gridy <= i && i <= cons.gridy + cons.gridh - 1) {
            weightSum += (cons.weighty / cons.gridh);
            weightCount++;
          }
        }
        weightY[i] = (weightCount > 0 ? weightSum / weightCount : 0);
      }
    }
    double sumWeightY = 0;
    for (int i = 0; i < rows; i++) {
      sumWeightY += weightY[i];
    }
    if (sumWeightY >= 1e-6) {
      double f = 1.0 / sumWeightY;
      for (int i = 0; i < rows; i++) {
        weightY[i] = weightY[i] * f;
      }
    }
  }

  /**
   * calculate grid cells (gaps are not included in the grid cell bounds)
   */
  Rectangle[][] layoutCellBounds(Dimension size, Insets insets) {
    int[] w = layoutSizes(size.width - insets.left - insets.right - Math.max(0, (cols - 1) * m_hgap), width, weightX);
    int[] h = layoutSizes(size.height - insets.top - insets.bottom - Math.max(0, (rows - 1) * m_vgap), height, weightY);
    Rectangle[][] cellBounds = new Rectangle[rows][cols];
    int y = insets.top;
    for (int r = 0; r < cellBounds.length; r++) {
      int x = insets.left;
      for (int c = 0; c < cellBounds[r].length; c++) {
        cellBounds[r][c] = new Rectangle(x, y, w[c], h[r]);
        x += w[c];
        x += m_hgap;
      }
      y += h[r];
      y += m_vgap;
    }
    return cellBounds;
  }

  private int[] layoutSizes(int targetSize, int[][] sizes, double[] weights) {
    int[] outSizes = new int[sizes.length];
    if (targetSize <= 0) {
      return new int[sizes.length];
    }
    int sumSize = 0;
    float[] tmpWeight = new float[weights.length];
    float sumWeight = 0;
    for (int i = 0; i < sizes.length; i++) {
      outSizes[i] = sizes[i][LogicalGridLayout.PREF_SIZE];
      sumSize += outSizes[i];
      tmpWeight[i] = (float) weights[i];
      /**
       * auto correction: if weight is 0 and min / max sizes are NOT equal then
       * set weight to 1; if weight<eps set it to 0
       */
      if (tmpWeight[i] < LogicalGridLayout.EPS) {
        if (sizes[i][LogicalGridLayout.MAX_SIZE] > sizes[i][LogicalGridLayout.MIN_SIZE]) {
          tmpWeight[i] = 1;
        }
        else {
          tmpWeight[i] = 0;
        }
      }
      sumWeight += tmpWeight[i];
    }
    // normalize weights
    if (sumWeight > 0) {
      for (int i = 0; i < tmpWeight.length; i++) {
        tmpWeight[i] = tmpWeight[i] / sumWeight;
      }
    }
    int deltaInt = targetSize - sumSize;
    // expand or shrink
    if (Math.abs(deltaInt) > 0) {
      // setup accumulators
      float[] accWeight = new float[tmpWeight.length];
      if (deltaInt > 0) {
        // expand
        boolean hasTargets = true;
        while (deltaInt > 0 && hasTargets) {
          hasTargets = false;
          for (int i = 0; i < outSizes.length && deltaInt > 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] < sizes[i][LogicalGridLayout.MAX_SIZE]) {
              hasTargets = true;
              accWeight[i] += tmpWeight[i];
              if (accWeight[i] > 0) {
                accWeight[i] -= 1;
                outSizes[i] += 1;
                deltaInt -= 1;
              }
            }
          }
        }
      }
      else {// delta<0
        // shrink
        boolean hasTargets = true;
        while (deltaInt < 0 && hasTargets) {
          hasTargets = false;
          for (int i = 0; i < outSizes.length && deltaInt < 0; i++) {
            if (tmpWeight[i] > 0 && outSizes[i] > sizes[i][LogicalGridLayout.MIN_SIZE]) {
              hasTargets = true;
              accWeight[i] += tmpWeight[i];
              if (accWeight[i] > 0) {
                accWeight[i] -= 1;
                outSizes[i] -= 1;
                deltaInt += 1;
              }
            }
          }
        }
      }
    }
    return outSizes;
  }

  private static int logicalWidthInPixel(ISwingEnvironment env, LogicalGridData cons) {
    int gridW = cons.gridw;
    return env.getFormColumnWidth() * gridW + env.getFormColumnGap() * Math.max(0, gridW - 1);
  }

  private static int logicalHeightInPixel(ISwingEnvironment env, LogicalGridData cons) {
    int gridH = cons.gridh;
    return env.getFormRowHeight() * gridH + env.getFormRowGap() * Math.max(0, gridH - 1);
  }

  private static Dimension uiSizeInPixel(Component comp) {
    return new Dimension(comp.getPreferredSize());
  }

}
