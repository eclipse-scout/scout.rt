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
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JInternalFrame;

import org.eclipse.scout.rt.ui.swing.ext.JInternalFrameEx;

class ViewModel {
  private Map<JInternalFrame, ViewElement> m_frameToViewMap;
  private Dimension m_minSize;
  private boolean m_mousePressed;

  public ViewModel() {
    m_frameToViewMap = new HashMap<JInternalFrame, ViewElement>();
  }

  public void rebuild(Cell[][] cells, CellSplit[][] rowSplits, CellSplit[][] colSplits) {
    m_frameToViewMap = new HashMap<JInternalFrame, ViewElement>();
    Map<CellSplit, Set<CellSplit>> rowSplitMS = new HashMap<CellSplit, Set<CellSplit>>();
    Map<CellSplit, Set<CellSplit>> colSplitMS = new HashMap<CellSplit, Set<CellSplit>>();
    //
    ComponentCell[][] cellMap = new ComponentCell[cells.length][cells[0].length];// row,col
    for (int r = 0; r < cellMap.length; r++) {
      for (int c = 0; c < cellMap[r].length; c++) {
        cellMap[r][c] = new ComponentCell();
      }
    }
    //
    mapCells(cells, cellMap);
    for (int r = 0; r < cells.length; r++) {
      for (int c = 0; c < cells[r].length; c++) {
        JInternalFrame frame = (JInternalFrame) cellMap[r][c].component;
        if (frame != null) {
          ViewElement v = m_frameToViewMap.get(frame);
          if (v == null) {
            v = new ViewElement(frame);
            m_frameToViewMap.put(frame, v);
          }
        }
      }
    }
    // prepare interconnections of splits and views
    ViewElement[][] viewMap = new ViewElement[cellMap.length][cellMap[0].length];
    for (ViewElement v : m_frameToViewMap.values()) {
      mapFrameToView(cellMap, viewMap, v, rowSplits, colSplits, rowSplitMS, colSplitMS);
    }
    // calculate interconnected dependencies
    for (ViewElement v : m_frameToViewMap.values()) {
      v.top = calculateCompleteViewSplit(rowSplitMS, v.top.getCellSplits(), viewMap);
      v.left = calculateCompleteViewSplit(colSplitMS, v.left.getCellSplits(), viewMap);
      v.bottom = calculateCompleteViewSplit(rowSplitMS, v.bottom.getCellSplits(), viewMap);
      v.right = calculateCompleteViewSplit(colSplitMS, v.right.getCellSplits(), viewMap);
    }
    /*
     * ticket 90942: don't validate size on unused splits
     * mark cell splits as used/unused to determine which one wins when splits were swapped
     */
    for (int r = 0; r < rowSplits.length; r++) {
      for (int c = 0; c < rowSplits[r].length; c++) {
        rowSplits[r][c].setUsedInModel(false);
      }
    }
    for (int r = 0; r < colSplits.length; r++) {
      for (int c = 0; c < colSplits[r].length; c++) {
        colSplits[r][c].setUsedInModel(false);
      }
    }
    for (ViewElement v : m_frameToViewMap.values()) {
      for (CellSplit c : v.top.getCellSplits()) {
        c.setUsedInModel(true);
      }
      for (CellSplit c : v.left.getCellSplits()) {
        c.setUsedInModel(true);
      }
      for (CellSplit c : v.bottom.getCellSplits()) {
        c.setUsedInModel(true);
      }
      for (CellSplit c : v.right.getCellSplits()) {
        c.setUsedInModel(true);
      }
    }
    //resolve swapped row splits
    for (int c = 0; c < rowSplits[0].length; c++) {
      for (int r = 1; r < rowSplits.length - 1; r++) {
        CellSplit a = rowSplits[r][c];
        CellSplit b = rowSplits[r + 1][c];
        if (a.getLocation() + 20 > b.getLocation()) {
          if (a.isUsedInModel() && b.isUsedInModel()) {
            //swap a and b
            int tmp = a.getLocation();
            a.setLocation(b.getLocation());
            b.setLocation(tmp);
          }
          else {
            //nop
          }
        }
      }
    }
    // validate min/max sizes of frames
    for (ViewElement v : m_frameToViewMap.values()) {
      v.validateMinMaxSizes();
    }
    // set valid scale directions on internal frames
    for (ViewElement v : m_frameToViewMap.values()) {
      JInternalFrame frame = v.getFrame();
      frame.putClientProperty(JInternalFrameEx.CLIENT_PROP_N_RESIZE_ENABLED, v.top.isFixed() ? Boolean.FALSE : Boolean.TRUE);
      frame.putClientProperty(JInternalFrameEx.CLIENT_PROP_W_RESIZE_ENABLED, v.left.isFixed() ? Boolean.FALSE : Boolean.TRUE);
      frame.putClientProperty(JInternalFrameEx.CLIENT_PROP_S_RESIZE_ENABLED, v.bottom.isFixed() ? Boolean.FALSE : Boolean.TRUE);
      frame.putClientProperty(JInternalFrameEx.CLIENT_PROP_E_RESIZE_ENABLED, v.right.isFixed() ? Boolean.FALSE : Boolean.TRUE);
    }
    // calculate minimum size
    HashMap<JInternalFrame, Dimension> minSizeCache = new HashMap<JInternalFrame, Dimension>();
    JInternalFrame lastFrame;
    int maxOfMinWidth = 0;
    for (int r = 0; r < cellMap.length; r++) {
      int minWidth = 0;
      lastFrame = null;
      for (int c = 0; c < cellMap[r].length; c++) {
        if (viewMap[r][c] != null) {
          JInternalFrame f = viewMap[r][c].getFrame();
          if (f != lastFrame) {
            lastFrame = f;
            Dimension d = minSizeCache.get(f);
            if (d == null) {
              d = f.getMinimumSize();
              minSizeCache.put(f, d);
            }
            minWidth += d.width;
          }
        }
      }
      maxOfMinWidth = Math.max(maxOfMinWidth, minWidth);
    }
    int maxOfMinHeight = 0;
    for (int c = 0; c < cellMap[0].length; c++) {
      int minHeight = 0;
      lastFrame = null;
      for (int r = 0; r < cellMap.length; r++) {
        if (viewMap[r][c] != null) {
          JInternalFrame f = viewMap[r][c].getFrame();
          if (f != lastFrame) {
            lastFrame = f;
            Dimension d = minSizeCache.get(f);
            if (d == null) {
              d = f.getMinimumSize();
              minSizeCache.put(f, d);
            }
            minHeight += d.height;
          }
        }
      }
      maxOfMinHeight = Math.max(maxOfMinHeight, minHeight);
    }
    m_minSize = new Dimension(maxOfMinWidth, maxOfMinHeight);
  }

  public Dimension getMinimumSize() {
    return new Dimension(m_minSize);
  }

  public boolean isMousePressed() {
    return m_mousePressed;
  }

  public void setMousePressed(boolean mousePressed) {
    m_mousePressed = mousePressed;
  }

  private void mapCells(Cell[][] cells, ComponentCell[][] map) {
    for (int r = 0; r < cells.length; r++) {
      for (int c = 0; c < cells[r].length; c++) {
        mapCell(cells[r][c], map);
      }
    }
  }

  /**
   * request desired cells on matrix according to priority
   */
  private void mapCell(Cell cell, ComponentCell[][] maps) {
    if (cell.getCellElementCount() > 0) {
      CellElement ce = cell.getCellElement(cell.getCellElementCount() - 1);
      for (int r = 0; r < maps.length; r++) {
        for (int c = 0; c < maps[r].length; c++) {
          mapComponent(ce.getComponent(), ce.getDistributionMatrix()[r][c], maps[r][c]);
        }
      }
    }
  }

  private void mapComponent(Component aComp, float aPrio, ComponentCell cc) {
    if (aComp.isVisible()) {
      if (aPrio > cc.prio) {
        cc.prio = aPrio;
        cc.component = aComp;
      }
    }
  }

  private void mapFrameToView(ComponentCell[][] cellMap, ViewElement[][] viewMap, ViewElement v, CellSplit[][] rowSplits, CellSplit[][] colSplits, Map<CellSplit, Set<CellSplit>> rowSplitMS, Map<CellSplit, Set<CellSplit>> colSplitMS) {
    JInternalFrame frame = v.getFrame();
    // find largest rectangle
    int largestSize = 0;
    int minR = 0;
    int maxR = 0;
    int minC = 0;
    int maxC = 0;
    for (int r = 0; r < cellMap.length; r++) {
      for (int c = 0; c < cellMap[r].length; c++) {
        if (cellMap[r][c].component == frame) {
          int h = 1;
          while (r + h < cellMap.length && cellMap[r + h][c].component == frame) {
            h++;
          }
          for (int tr = r; tr < r + h; tr++) {
            int w = 1;
            while (c + w < cellMap[tr].length && cellMap[tr][c + w].component == frame) {
              w++;
            }
            if (w * h > largestSize) {
              minR = r;
              maxR = r + h - 1;
              minC = c;
              maxC = c + w - 1;
              largestSize = w * h;
            }
          }
        }
      }
    }
    if (largestSize > 0) {
      for (int r = minR; r <= maxR; r++) {
        for (int c = minC; c <= maxC; c++) {
          viewMap[r][c] = v;
        }
      }
      HashSet<CellSplit> tieList = new HashSet<CellSplit>();
      // left (use full height splitter, layout is column-based)
      tieList.clear();
      for (int r = 0; r <= colSplits.length - 1; r++) {
        tieList.add(colSplits[r][minC]);
      }
      v.left = tieCellSplits(colSplitMS, tieList, viewMap);
      // right (use full height splitter, layout is column-based)
      tieList.clear();
      for (int r = 0; r <= colSplits.length - 1; r++) {
        tieList.add(colSplits[r][maxC + 1]);
      }
      v.right = tieCellSplits(colSplitMS, tieList, viewMap);
      // top
      tieList.clear();
      for (int c = minC; c <= maxC; c++) {
        tieList.add(rowSplits[minR][c]);
      }
      v.top = tieCellSplits(rowSplitMS, tieList, viewMap);
      // bottom
      tieList.clear();
      for (int c = minC; c <= maxC; c++) {
        tieList.add(rowSplits[maxR + 1][c]);
      }
      v.bottom = tieCellSplits(rowSplitMS, tieList, viewMap);
    }
  }

  private ViewSplit tieCellSplits(Map<CellSplit, Set<CellSplit>> splitMS, Set<CellSplit> list, ViewElement[][] viewMap) {
    HashSet<CellSplit> splitSet = new HashSet<CellSplit>();
    splitSet.addAll(list);
    for (CellSplit cs : list) {
      Set<CellSplit> subSet = splitMS.get(cs);
      if (subSet != null) {
        splitSet.addAll(subSet);
      }
    }
    for (CellSplit cs : splitSet) {
      splitMS.put(cs, splitSet);
    }
    ViewSplit vs = new ViewSplit(splitSet);
    return vs;
  }

  private ViewSplit calculateCompleteViewSplit(Map<CellSplit, Set<CellSplit>> splitMS, Set<CellSplit> splitSet, ViewElement[][] viewMap) {
    HashSet<CellSplit> completeSet = new HashSet<CellSplit>();
    for (CellSplit split : splitSet) {
      Set<CellSplit> subSet = splitMS.get(split);
      if (subSet != null) {
        completeSet.addAll(subSet);
      }
    }
    HashSet<ViewElement> leftViews = new HashSet<ViewElement>();
    HashSet<ViewElement> rightViews = new HashSet<ViewElement>();
    for (CellSplit split : completeSet) {
      Cell cell = split.getLeftItem();
      if (cell != null) {
        ViewElement v = viewMap[cell.getRow()][cell.getColumn()];
        if (v != null) {
          leftViews.add(v);
        }
      }
      cell = split.getRightItem();
      if (cell != null) {
        ViewElement v = viewMap[cell.getRow()][cell.getColumn()];
        if (v != null) {
          rightViews.add(v);
        }
      }
    }
    //
    ViewSplit vs = new ViewSplit(completeSet, leftViews, rightViews);
    return vs;
  }

  public Rectangle getBoundsFor(JInternalFrame frame) {
    ViewElement v = m_frameToViewMap.get(frame);
    if (v != null && v.left != null && v.right != null && v.top != null && v.bottom != null) {
      return new Rectangle(v.left.getLocation(), v.top.getLocation(), v.right.getLocation() - v.left.getLocation(), v.bottom.getLocation() - v.top.getLocation());
    }
    else {
      return new Rectangle(0, 0, 0, 0);
    }
  }

  public void iconify(JInternalFrame f, Rectangle newR) {
    ViewElement view = m_frameToViewMap.get(f);
    if (view != null) {
      view.resize(newR);
    }
  }

  /**
   * resize frame exactly as it is requested Example: when expanding to bottom,
   * but bottom is fixed nothing is done
   */
  public void resize(JInternalFrame f, Rectangle newR) {
    //only accept a frame resize from user activity (mouse dragging)
    if (isMousePressed()) {
      ViewElement view = m_frameToViewMap.get(f);
      if (view != null) {
        view.resize(newR);
      }
    }
  }

  /**
   * resize frame as it is requested, but allow opposite expanding/shrinking
   * Example: when expanding to bottom, but bottom is fixed expand to top if
   * possible
   */
  public void fitSize(JInternalFrame f, Dimension size) {
    ViewElement view = m_frameToViewMap.get(f);
    if (view != null) {
      view.fitSize(size);
    }
  }

  static class ComponentCell {
    float prio;
    Component component;
  }
}
