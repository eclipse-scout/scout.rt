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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * Dynamic layout using logical grid data {@link LogicalGridData} to arrange
 * fields. The grid data per field can be passed when adding the component to
 * the container or set as client property with name {@link LogicalGridData#CLIENT_PROPERTY_NAME}.
 */
public class LogicalGridLayout extends AbstractLayoutManager2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogicalGridLayout.class);

  public static final float EPS = 1E-6f;

  private ISwingEnvironment m_env;
  private boolean m_debug;
  private int m_hgap;
  private int m_vgap;
  private LogicalGridLayoutInfo m_info;

  public LogicalGridLayout(ISwingEnvironment env, int hgap, int vgap) {
    m_env = env;
    m_hgap = hgap;
    m_vgap = vgap;
  }

  public void setDebug(boolean b) {
    m_debug = b;
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp, null);
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
    if (constraints instanceof LogicalGridData) {
      ((JComponent) comp).putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, constraints);
    }
    super.addLayoutComponent(comp, constraints);
  }

  private LogicalGridData getLayoutDataByRef(Component comp) {
    LogicalGridData data = (LogicalGridData) ((JComponent) comp).getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    if (data == null) {
      data = new LogicalGridData();
      LOG.error("missing clientProperty " + LogicalGridData.CLIENT_PROPERTY_NAME + " in " + comp.getName() + "/" + comp.getClass() + " parent is " + comp.getParent().getName() + "/" + comp.getParent().getClass());
    }
    return data;
  }

  @Override
  public float getLayoutAlignmentX(Container parent) {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container parent) {
    return 0;
  }

  @Override
  protected void validateLayout(Container parent) {
    ArrayList<Component> visibleComps = new ArrayList<Component>();
    ArrayList<LogicalGridData> visibleCons = new ArrayList<LogicalGridData>();
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component comp = parent.getComponent(i);
      if (comp.isVisible()) {
        visibleComps.add(comp);
        LogicalGridData cons = getLayoutDataByRef(comp);
        cons.validate();
        visibleCons.add(cons);
      }
    }
    m_info = new LogicalGridLayoutInfo(m_env, visibleComps.toArray(new Component[visibleComps.size()]), visibleCons.toArray(new LogicalGridData[visibleCons.size()]), m_hgap, m_vgap);
  }

  public void dumpLayoutInfo(Container parent) {
    dumpLayoutInfo(parent, new PrintWriter(System.out));
  }

  public void dumpLayoutInfo(Container parent, PrintWriter out) {
    out.println("**************** LogicalGridLayout[" + parent.getName() + "]");
    Component[] c = m_info.components;
    String[] names = new String[c.length];
    String[] constraints = new String[c.length];
    for (int i = 0; i < c.length; i++) {
      String cls = c[i].getClass().getName();
      int dot = Math.max(cls.lastIndexOf('.'), cls.lastIndexOf('>'));
      if (dot >= 0) cls = cls.substring(dot + 1);
      names[i] = cls + " (" + c[i].getName() + (c[i].isVisible() ? "" : " invisible") + ")";
      constraints[i] = m_info.gridDatas[i].toString();
    }
    out.println("Layout Info");
    out.println("  hgap=" + m_hgap);
    out.println("  vgap=" + m_vgap);
    out.println("  size=" + parent.getSize());
    out.println("  minSize=" + getLayoutSize(parent, 0));
    out.println("  prfSize=" + getLayoutSize(parent, 1));
    out.println("  maxSize=" + getLayoutSize(parent, 2));
    out.println("  insets=" + parent.getInsets());
    out.println("  components=" + dump(names));
    out.println("  constraints=" + dump(constraints));
    out.println("  cols=" + m_info.cols);
    out.println("  rows=" + m_info.rows);
    out.println("  col-width=" + dump(m_info.width));
    out.println("  row-height=" + dump(m_info.height));
    out.println("  col-weightX=" + dump(m_info.weightX));
    out.println("  row-weightY=" + dump(m_info.weightY));
    Rectangle[][] cellBounds = layoutCellBounds(parent.getSize(), parent.getInsets());
    if (cellBounds != null) {
      for (int row = 0; row < cellBounds.length; row++) {
        for (int col = 0; col < cellBounds[row].length; col++) {
          out.println("  cell[" + row + "][" + col + "]=" + cellBounds[row][col]);
        }
      }
      if (cellBounds.length > 0) {
        Rectangle last = cellBounds[cellBounds.length - 1][cellBounds[cellBounds.length - 1].length - 1];
        if (parent.getWidth() > 0 && parent.getHeight() > 0) {
          if (last.x + last.width > parent.getWidth() - parent.getInsets().left - parent.getInsets().right) {
            out.println("!!! width too large: " + (last.x + last.width) + " > " + (parent.getWidth() - parent.getInsets().left - parent.getInsets().right));
          }
          if (last.y + last.height > parent.getHeight() - parent.getInsets().top - parent.getInsets().bottom) {
            out.println("!!! height too large: " + (last.y + last.height) + " > " + (parent.getHeight() - parent.getInsets().top - parent.getInsets().bottom));
          }
        }
      }
    }
    out.flush();
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    Dimension dim = new Dimension();
    // w
    int useCount = 0;
    for (int i = 0; i < m_info.cols; i++) {
      int w = m_info.width[i][sizeflag];
      if (useCount > 0) {
        dim.width += m_hgap;
      }
      dim.width += w;
      useCount++;
    }
    // h
    useCount = 0;
    for (int i = 0; i < m_info.rows; i++) {
      int h = m_info.height[i][sizeflag];
      if (useCount > 0) {
        dim.height += m_vgap;
      }
      dim.height += h;
      useCount++;
    }
    if (dim.width > 0 && dim.height > 0) {
      Insets insets = parent.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
    }
    return dim;
  }

  @Override
  public void layoutContainer(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      if (m_debug || LOG.isDebugEnabled()) {
        dumpLayoutInfo(parent);
      }
      Dimension size = parent.getSize();
      Rectangle[][] cellBounds = layoutCellBounds(size, parent.getInsets());
      /*
       * necessary as workaround for awt bug: when component does not change
       * size, its reported minimumSize, preferredSize and maximumSize are
       * cached instead of beeing calculated using layout manager
       */
      for (Component c : parent.getComponents()) {
        c.setBounds(0, 0, 0, 0);
      }
      // bounds
      int n = m_info.components.length;
      for (int i = 0; i < n; i++) {
        Component comp = m_info.components[i];
        LogicalGridData data = m_info.gridDatas[i];
        Rectangle r1 = cellBounds[data.gridy][data.gridx];
        Rectangle r2 = cellBounds[data.gridy + data.gridh - 1][data.gridx + data.gridw - 1];
        Rectangle r = r1.union(r2);
        if (data.topInset > 0) {
          r.y += data.topInset;
          r.height -= data.topInset;
        }
        if (data.fillHorizontal && data.fillVertical) {
          // ok
        }
        else {
          Dimension d = comp.getPreferredSize();
          if (!data.fillHorizontal) {
            if (d.width < r.width) {
              int delta = r.width - d.width;
              r.width = d.width;
              if (data.horizontalAlignment == 0) {
                r.x += delta / 2;
              }
              else if (data.horizontalAlignment > 0) {
                r.x += delta;
              }
            }
          }
          if (!data.fillVertical) {
            if (d.height < r.height) {
              int delta = r.height - d.height;
              if (data.heightHint == 0) {
                r.height = d.height;
              }
              else {
                r.height = data.heightHint;
              }
              if (data.verticalAlignment == 0) {
                r.y += delta / 2;
              }
              else if (data.verticalAlignment > 0) {
                r.y += delta;
              }
            }
          }
        }
        comp.setBounds(r);
      }
    }
  }

  /**
   * calculate grid cells (gaps are not included in the grid cell bounds)
   */
  private Rectangle[][] layoutCellBounds(Dimension size, Insets insets) {
    int[] w = layoutSizes(size.width - insets.left - insets.right - Math.max(0, (m_info.cols - 1) * m_hgap), m_info.width, m_info.weightX);
    int[] h = layoutSizes(size.height - insets.top - insets.bottom - Math.max(0, (m_info.rows - 1) * m_vgap), m_info.height, m_info.weightY);
    Rectangle[][] cellBounds = new Rectangle[m_info.rows][m_info.cols];
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
      outSizes[i] = sizes[i][PREF_SIZE];
      sumSize += outSizes[i];
      tmpWeight[i] = (float) weights[i];
      /**
       * auto correction: if weight is 0 and min / max sizes are NOT equal then
       * set weight to 1; if weight<eps set it to 0
       */
      if (tmpWeight[i] < EPS) {
        if (sizes[i][MAX_SIZE] > sizes[i][MIN_SIZE]) {
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
            if (tmpWeight[i] > 0 && outSizes[i] < sizes[i][MAX_SIZE]) {
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
            if (tmpWeight[i] > 0 && outSizes[i] > sizes[i][MIN_SIZE]) {
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

}
