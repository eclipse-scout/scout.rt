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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

class ViewSplit {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ViewSplit.class);

  private Set<CellSplit> m_cellSplits;
  private Set<ViewElement> m_leftViews;
  private Set<ViewElement> m_rightViews;

  ViewSplit(Set<CellSplit> cellSplits) {
    m_cellSplits = cellSplits;
  }

  public ViewSplit(Set<CellSplit> cellSplits, Set<ViewElement> leftViews, Set<ViewElement> rightViews) {
    m_cellSplits = cellSplits;
    m_leftViews = leftViews;
    m_rightViews = rightViews;
  }

  public Set<CellSplit> getCellSplits() {
    return m_cellSplits;
  }

  public Set<ViewElement> getLeftViews() {
    return m_leftViews;
  }

  public Set<ViewElement> getRightViews() {
    return m_rightViews;
  }

  public int getLocation() {
    int i = 0;
    for (CellSplit s : m_cellSplits) {
      i += s.getLocation();
    }
    i = i / Math.max(1, m_cellSplits.size());
    return i;
  }

  public void setLocation(int loc) {
    if (!isFixed()) {
      for (CellSplit s : m_cellSplits) {
        s.setLocation(loc);
      }
    }
  }

  /**
   * left=top, right=bottom
   * 
   * @return amount that was effectively moved
   */
  public int move(int delta, boolean force) {
    if (delta != 0) {
      int limit;
      if (force) {
        limit = Math.abs(delta);
      }
      else {
        boolean right = delta > 0;
        // check min/max sizes
        int leftSideLimit = 0;
        int rightSideLimit = 0;
        if (!isFixed()) {
          leftSideLimit = 10240;
          rightSideLimit = 10240;
          for (ViewElement v : m_leftViews) {
            int[]/* distance-to-min,distance-to-max*/bounds = getResizeDistances(v, isRowSplit() ? v.top : v.left);
            int distanceToMin = bounds[0];
            int distanceToMax = bounds[1];
            if (right) {
              // use distanceToMax
              leftSideLimit = Math.min(leftSideLimit, distanceToMax);
            }
            else {
              // use negative distanceToMin
              leftSideLimit = Math.min(leftSideLimit, -distanceToMin);
            }
          }
          for (ViewElement v : m_rightViews) {
            int[]/* distance-to-min,distance-to-max*/bounds = getResizeDistances(v, isRowSplit() ? v.bottom : v.right);
            int distanceToMin = bounds[0];
            int distanceToMax = bounds[1];
            if (right) {
              // use negative distanceToMin
              rightSideLimit = Math.min(rightSideLimit, -distanceToMin);
            }
            else {
              // use distanceToMax
              rightSideLimit = Math.min(rightSideLimit, distanceToMax);
            }
          }
        }
        limit = Math.min(leftSideLimit, rightSideLimit);
        if (limit > 0) {
          if (right) {
            if (delta > limit) {
              delta = limit;
            }
          }
          else {
            if (delta < -limit) {
              delta = -limit;
            }
          }
          setLocation(getLocation() + delta);
          return delta;
        }
        else {
          return 0;
        }
      }
      setLocation(getLocation() + delta);
      return delta;
    }
    return 0;
  }

  public boolean isFixed() {
    for (CellSplit s : m_cellSplits) {
      if (s.isFixed()) return true;
    }
    return false;
  }

  public boolean isRowSplit() {
    for (CellSplit s : m_cellSplits) {
      if (s.isRowSplit()) return true;
    }
    return false;
  }

  private int[]/*distance-to-min,distance-to-max*/getResizeDistances(ViewElement v, ViewSplit oppositeSplit) {
    Component comp = v.getFrame();
    if (comp == null || oppositeSplit == null) {
      return new int[]{0, 0};
    }
    Dimension[] d = SwingLayoutUtility.getValidatedSizes(comp);
    /*
     * ticket 90942, detail pane initially only has 20px height
     * The size of the JInternalFrame is irrelevant and may be different than the effective split size,
     * therefore strictly use the current size of the split distance
     * [old inaccurate: Dimension s = comp.getSize();]
     */
    int splitSize = Math.abs(oppositeSplit.getLocation() - this.getLocation());
    if (isRowSplit()) {
      return getResizeDistances(d[0].height, splitSize, d[2].height);
    }
    else {
      return getResizeDistances(d[0].width, splitSize, d[2].width);
    }
  }

  private int[]/* distance-to-min,distance-to-max */getResizeDistances(int min, int value, int max) {
    if (value < min) {
      return new int[]{0, max - value};
    }
    else if (value > max) {
      return new int[]{min - value, 0};
    }
    else {
      return new int[]{min - value, max - value};
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + (isFixed() ? "fixed " : "") + getLocation() + "]";
  }
}
