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

public class LogicalGridLayoutInfo {

  private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);

  private ISwingEnvironment m_env;

  LogicalGridData[/* component count */] gridDatas;
  Component[/* component count */] m_components;
  int cols;/* number of cells horizontally */
  int rows;/* number of cells vertically */
  int[/* column */][/* min,pref,max */] width;
  int[/* row */][/* min,pref,max */] height;
  double[/* column */] weightX;
  double[/* row */] weightY;
  private int m_hgap;
  private int m_vgap;
  private Rectangle[][] m_cellBounds;

  private boolean m_useLogicalPrefSize;
  private Dimension m_sizePref;
  private Dimension m_sizeMin;
  private Dimension m_sizeMax;

  /**
   * @param env
   *          Swing-UI configuration.
   * @param components
   *          the components to be laid out in the container.
   * @param cons
   *          logical GridData constraints in the order of the container's components.
   * @param hgap
   *          horizontal gap between the cells of the grid (gap in between of adjacent rows).
   * @param vgap
   *          vertical gap between the cells of the grid (gap in between of adjacent columns).
   * @param useLogicalPrefSize
   *          <code>true</code> to only use logical sizes when calculating the preferred size of the grid (default),
   *          <code>false</code> to respect the parent's dimension when calculating the preferred width of the grid.
   *          This is vital if a parent container calculates its height based on the children's width (e.g.
   *          tab-row-extent in a tabbed pane) [Bugzilla 410306].
   */
  LogicalGridLayoutInfo(ISwingEnvironment env, Component[] components, LogicalGridData[] cons, int hgap, int vgap, boolean useLogicalPrefSize) {
    m_env = env;
    m_components = components;
    m_hgap = hgap;
    m_vgap = vgap;
    m_useLogicalPrefSize = useLogicalPrefSize;
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
      if (gd.gridx < 0) {
        gd.gridx = 0;
      }
      if (gd.gridy < 0) {
        gd.gridy = 0;
      }
      if (gd.gridw < 1) {
        gd.gridw = 1;
      }
      if (gd.gridh < 1) {
        gd.gridh = 1;
      }
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
    initializeInfo();
  }

  private void initializeInfo() {
    int compCount = m_components.length;
    Dimension[] compSize = new Dimension[compCount];
    // cache component sizes and cleanup constraints
    for (int i = 0; i < compCount; i++) {
      Component comp = m_components[i];
      LogicalGridData cons = gridDatas[i];
      Dimension d = uiSizeInPixel(comp);
      if (cons.widthHint > 0) {
        d.width = cons.widthHint;
      }
      if (cons.heightHint > 0) {
        d.height = cons.heightHint;
      }
      compSize[i] = d;
      if (cons.gridx < 0) {
        cons.gridx = 0;
      }
      if (cons.gridy < 0) {
        cons.gridy = 0;
      }
      if (cons.gridw < 1) {
        cons.gridw = 1;
      }
      if (cons.gridh < 1) {
        cons.gridh = 1;
      }
      if (cons.gridx >= cols) {
        cons.gridx = cols - 1;
      }
      if (cons.gridy >= rows) {
        cons.gridy = rows - 1;
      }
      if (cons.gridx + cons.gridw - 1 >= cols) {
        cons.gridw = cols - cons.gridx;
      }
      if (cons.gridy + cons.gridh >= rows) {
        cons.gridh = rows - cons.gridy;
      }
    }
    initializeColumns(compSize);
    initializeRows(compSize);
  }

  /**
   * Calculates the logical widths and weights of the grid columns.
   *
   * @param compSize
   *          the logical dimensions of the components in the grid.
   */
  private void initializeColumns(Dimension[] compSize) {
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
          prefw = logicalWidthInPixel(m_env, cons);
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
          distWidth = cons.widthHint - spanWidth - (hSpan - 1) * m_hgap;
        }
        else if (cons.useUiWidth) {
          distWidth = compSize[i].width - spanWidth - (hSpan - 1) * m_hgap;
        }
        else {
          distWidth = logicalWidthInPixel(m_env, cons) - spanWidth - (hSpan - 1) * m_hgap;
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
        width[i][SwingLayoutUtility.MIN] = prefWidths[i]; // do not set to 0 especially because of field-labels which get truncated too early in small forms (e.g. search forms) or even worse, forms get messy if having fields that span multiple columns together with other fields that don't.
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

  /**
   * Calculates the logical heights and weights of the grid rows.
   *
   * @param compSize
   *          the logical dimensions of the components in the grid.
   */
  private void initializeRows(Dimension[] compSize) {
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
          prefh = logicalHeightInPixel(m_env, cons);
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
          distHeight = cons.heightHint - spanHeight - (vspan - 1) * m_vgap;
        }
        else if (cons.useUiHeight) {
          distHeight = compSize[i].height - spanHeight - (vspan - 1) * m_vgap;
        }
        else {
          distHeight = logicalHeightInPixel(m_env, cons) - spanHeight - (vspan - 1) * m_vgap;
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
        height[i][SwingLayoutUtility.MIN] = 0; // must be exactly 0!
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
   * Computes the grid with its cells aligned to the parent's dimension. Thereby, gaps are not included in the grid cell
   * bounds. Also, the minimal, maximal and preferred extent of the grid is calculated.
   *
   * @param parentSize
   *          the actual size of the parent container.
   * @param insets
   *          the insets of the parent container.
   * @param rootShowing
   *          Indicates whether the root container is showing on screen. This is used do determine the initial extent
   *          of the grid.
   * @return the bounds of the cells.
   */
  Rectangle[][] layoutCellBounds(Dimension parentSize, Insets insets, boolean rootShowing) {
    boolean initialization = !rootShowing && ZERO_DIMENSION.equals(parentSize);

    /*
     * 1. Determine the column widths and row heights based on the parent's dimension.
     */
    int[] w;
    int[] h;
    if (initialization) {
      // The container does not specify a concrete dimension yet (e.g. during initialization).
      w = new int[width.length];
      h = new int[height.length];
    }
    else {
      // Calculate the column widths and row heights based on the parent's dimension.
      w = layoutSizes(parentSize.width - insets.left - insets.right - Math.max(0, (cols - 1) * m_hgap), width, weightX);
      h = layoutSizes(parentSize.height - insets.top - insets.bottom - Math.max(0, (rows - 1) * m_vgap), height, weightY);
    }

    /*
     * 2. Construct the the grid.
     */
    m_cellBounds = new Rectangle[rows][cols];
    int y = insets.top;
    for (int r = 0; r < m_cellBounds.length; r++) {
      int x = insets.left;
      for (int c = 0; c < m_cellBounds[r].length; c++) {
        m_cellBounds[r][c] = new Rectangle(x, y, w[c], h[r]);
        x += w[c];
        x += m_hgap;
      }
      y += h[r];
      y += m_vgap;
    }

    /*
     * 3. Compute the minimal, maximal and preferred extent of the grid.
     */
    if (initialization || m_useLogicalPrefSize) {
      // Use logical sizes if the root container is not showing on screen to determine the container's initial size.
      int[] logicalColWidths = extractSizes(width, LogicalGridLayout.PREF_SIZE);
      int[] logicalRowHeights = extractSizes(height, LogicalGridLayout.PREF_SIZE);
      m_sizePref = calculateGridDimension(logicalColWidths, logicalRowHeights, insets, m_hgap, m_vgap);
    }
    else {
      // Width: Use calculated widths that respect the container's dimension. This is vital for containers which rely on the children's width to determine their height, e.g. the tabbed pane do determine the tab-row-extent.
      // Height: Use logical heights because this is of interest for the parent container, e.g. for a scrolling container to correctly determine the viewport-size.
      int[] logicalRowheights = extractSizes(height, LogicalGridLayout.PREF_SIZE);
      m_sizePref = calculateGridDimension(w, logicalRowheights, insets, m_hgap, m_vgap);
    }
    m_sizeMin = calculateGridDimension(extractSizes(width, LogicalGridLayout.PREF_SIZE), extractSizes(height, LogicalGridLayout.PREF_SIZE), insets, m_hgap, m_vgap); // use the logical size as minimal size to not shrink to empty space.
    m_sizeMax = calculateGridDimension(extractSizes(width, LogicalGridLayout.MAX_SIZE), extractSizes(height, LogicalGridLayout.MAX_SIZE), insets, m_hgap, m_vgap);

    return m_cellBounds;
  }

  /**
   * To get the sizes of the given type (MIN, MAX, PREF).
   */
  private static int[] extractSizes(int[][] sizes, int sizeFlag) {
    int[] result = new int[sizes.length];
    for (int i = 0; i < sizes.length; i++) {
      result[i] = sizes[i][sizeFlag];
    }
    return result;
  }

  /**
   * Calculates the grid-dimension spanned by all the cells.
   */
  private static Dimension calculateGridDimension(int[] colWidths, int[] rowHeights, Insets insets, int hgap, int vgap) {
    int width = 0;
    int height = 0;

    // Calculate the total width.
    if (colWidths.length > 0) {
      for (int colWidth : colWidths) {
        width += colWidth;
      }

      width += (colWidths.length - 1) * hgap; // Horizontal gaps in between of the cells.
      width += insets.left + insets.right; // Insets to the left and right.
    }

    // Calculate the total height.
    if (rowHeights.length > 0) {
      for (int rowHeight : rowHeights) {
        height += rowHeight;
      }

      height += (rowHeights.length - 1) * vgap; // Vertical gaps in between of the cells.
      height += insets.top + insets.bottom; // Insets to the top and bottom.
    }

    return new Dimension(width, height);
  }

  /**
   * @return the cell bounds of the components.
   * @see #layoutCellBounds(Dimension, Insets)
   */
  public Rectangle[][] getCellBounds() {
    return m_cellBounds;
  }

  /**
   * @return the grid-dimension taken by all the containing cells.
   */
  public Dimension getGridDimension(int sizeFlag) {
    switch (sizeFlag) {
      case LogicalGridLayout.PREF_SIZE:
        return m_sizePref;
      case LogicalGridLayout.MIN_SIZE:
        return m_sizeMin;
      case LogicalGridLayout.MAX_SIZE:
        return m_sizeMax;
      default:
        throw new IllegalArgumentException(String.format("Unsupported size flag: %s", sizeFlag));
    }
  }

  /**
   * Calculates the components's sizes in respect to the given size of the container.
   *
   * @param targetSize
   *          the size specified by the container.
   * @param sizes
   *          sizes as specified in the logical grid layout.
   * @param weights
   *          weights as specified in the logical grid layout.
   * @return
   */

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
        // Expand the components because not taking all the available space.
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
      else {
        // Shrink the content because not fitting into the parent container.
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
