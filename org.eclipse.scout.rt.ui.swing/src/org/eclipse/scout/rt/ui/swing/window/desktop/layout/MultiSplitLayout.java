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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

/**
 * Only works together with the PerspectiveDesktopManager to support
 * Eclipse-like JInternalFrame handling inside JDesktopPane This layout is a 3x3
 * cell layout with variable splitters between every cell
 */
public class MultiSplitLayout extends AbstractLayoutManager2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MultiSplitLayout.class);
  private static final String CONSTRAINT_PROPERTY_NAME = "MultiSplitLayoutConstraints";
  private static final int UNLIMITED = 10240;

  private final Cell[][] m_cells = new Cell[3][3];
  /*
   * top and bottom splits are fixed
   */
  private final CellSplit[][] m_rowSplits = new CellSplit[4][3];
  /*
   * left and right splits are fixed
   */
  private final CellSplit[][] m_colSplits = new CellSplit[3][4];
  private final ViewModel m_model;
  private final MouseListener m_mouseListener;
  private final IMultiSplitStrategy m_columnSplitStrategy;
  // cache
  private Dimension[] m_sizes;

  public MultiSplitLayout(IMultiSplitStrategy columnSplitStrategy) {
    m_columnSplitStrategy = columnSplitStrategy != null ? columnSplitStrategy : new DefaultColumnSplitStrategy();
    //
    for (int r = 0; r < m_cells.length; r++) {
      for (int c = 0; c < m_cells[r].length; c++) {
        m_cells[r][c] = new Cell(r, c, true);
      }
    }
    //
    for (int r = 0; r < m_rowSplits.length; r++) {
      for (int c = 0; c < m_rowSplits[r].length; c++) {
        int[] top = new int[]{r - 1, c};
        int[] bottom = new int[]{r, c};
        if (r == 0) {
          top = null;
        }
        else if (r == m_rowSplits.length - 1) {
          bottom = null;
        }
        m_rowSplits[r][c] = new CellSplit(true, r, c, r == 0 || r == m_rowSplits.length - 1, top != null ? m_cells[top[0]][top[1]] : null, bottom != null ? m_cells[bottom[0]][bottom[1]] : null, 200 * r, null);
      }
    }
    for (int r = 0; r < m_colSplits.length; r++) {
      for (int c = 0; c < m_colSplits[r].length; c++) {
        int[] left = new int[]{r, c - 1};
        int[] right = new int[]{r, c};
        if (c == 0) {
          left = null;
        }
        else if (c == m_colSplits[r].length - 1) {
          right = null;
        }
        m_colSplits[r][c] = new CellSplit(false, r, c, c == 0 || c == m_colSplits[r].length - 1, left != null ? m_cells[left[0]][left[1]] : null, right != null ? m_cells[right[0]][right[1]] : null, 0, m_columnSplitStrategy);
      }
    }
    m_model = new ViewModel();
    //
    m_mouseListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        m_model.setMousePressed(true);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        m_model.setMousePressed(false);
      }
    };
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp, null);
  }

  @Override
  public void addLayoutComponent(Component comp, Object cons) {
    if (comp instanceof JInternalFrame) {
      JInternalFrame f = (JInternalFrame) comp;
      f.addMouseListener(m_mouseListener);
      if (cons == null) {
        cons = f.getClientProperty(CONSTRAINT_PROPERTY_NAME);
      }
      if (cons instanceof MultiSplitLayoutConstraints) {
        f.putClientProperty(CONSTRAINT_PROPERTY_NAME, cons);
        MultiSplitLayoutConstraints mcons = (MultiSplitLayoutConstraints) cons;
        m_cells[mcons.row][mcons.col].addView(comp, mcons.distributionMap);
      }
      else {
        throw new IllegalArgumentException("expected layout constraints of type MultiSplitLayoutConstraints for " + f.getTitle());
      }
    }
    invalidateLayout();
  }

  @Override
  public void removeLayoutComponent(Component comp) {
    if (comp instanceof JInternalFrame) {
      comp.removeMouseListener(m_mouseListener);
      for (int r = 0; r < m_cells.length; r++) {
        for (int c = 0; c < m_cells[r].length; c++) {
          m_cells[r][c].removeView(comp);
        }
      }
    }
    invalidateLayout();
  }

  @Override
  public float getLayoutAlignmentX(Container parent) {
    return 0.5f;
  }

  @Override
  public float getLayoutAlignmentY(Container parent) {
    return 0.5f;
  }

  @Override
  protected void validateLayout(Container parent) {
    // validate row splits
    for (int c = 0; c < m_rowSplits[0].length; c++) {
      // adapt to parent height
      int curHeight = m_rowSplits[m_rowSplits.length - 1][c].getLocation();
      int height = parent.getHeight();
      if (height > curHeight) {
        int dy = height - curHeight;
        for (int k = m_rowSplits.length - 2; k < m_rowSplits.length; k++) {
          CellSplit split = m_rowSplits[k][c];
          split.setLocation(split.getLocation() + dy);
        }
      }
      else if (height < curHeight) {
        int maxLoc = height;
        for (int r = m_rowSplits.length - 1; r > 0; r--) {
          if (m_rowSplits[r][c].getLocation() > maxLoc) {
            m_rowSplits[r][c].setLocation(maxLoc);
          }
          else {
            break;
          }
          maxLoc = Math.max(0, maxLoc - 20);
        }
      }
    }
    //validate column splits
    m_columnSplitStrategy.updateSpan(parent.getWidth());
    // update model
    m_model.rebuild(m_cells, m_rowSplits, m_colSplits);
    // cached sizes
    m_sizes = new Dimension[3];
    Insets insets = parent.getInsets();
    Dimension minSize = m_model.getMinimumSize();
    minSize.width += insets.left + insets.right;
    minSize.height += insets.top + insets.bottom;
    m_sizes[0] = minSize;
    m_sizes[1] = new Dimension(Math.max(800, m_sizes[0].width), Math.max(600, m_sizes[0].height));
    m_sizes[2] = new Dimension(10240, 10240);
  }

  @Override
  protected Dimension getLayoutSize(Container parent, int sizeflag) {
    return m_sizes[sizeflag];
  }

  @Override
  public void layoutContainer(Container parent) {
    verifyLayout(parent);
    synchronized (parent.getTreeLock()) {
      // are some frames maximized?
      JInternalFrame maximizedFrame = null;
      for (int i = 0, ni = parent.getComponentCount(); i < ni; i++) {
        Component comp = parent.getComponent(i);
        if (comp instanceof JInternalFrame) {
          if (((JInternalFrame) comp).isMaximum()) {
            if (comp.isVisible()) {
              maximizedFrame = (JInternalFrame) comp;
              break;
            }
            else {
              try {
                ((JInternalFrame) comp).setMaximum(false);
              }
              catch (Exception e) {
                //
              }
            }
          }
        }
      }
      //
      if (maximizedFrame == null) {
        for (int i = 0, ni = parent.getComponentCount(); i < ni; i++) {
          Component comp = parent.getComponent(i);
          if (comp instanceof JInternalFrame) {
            Rectangle r = m_model.getBoundsFor((JInternalFrame) comp);
            // this is necessary to avoid a repaint of the whole screen each
            // time the layout is invalidated
            // (what happens a lot!)
            if (r.x != comp.getX() || r.y != comp.getY() || r.width != comp.getWidth() || r.height != comp.getHeight()) {
              comp.setBounds(r);
            }
          }
        }
      }
    }
  }

  public ViewModel getModel(Container parent) {
    verifyLayout(parent);
    return m_model;
  }

}
