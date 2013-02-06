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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Renders the layout of the compact calendar widget.
 * 
 * @author awe
 */
public class SmallCalendarLayout implements LayoutManager2 {

  private DateChooser m_dateChooser;
  private Dimension m_prefSize;
  private Dimension m_cellSize;

  public SmallCalendarLayout(DateChooser dateChooser) {
    m_dateChooser = dateChooser;
    m_prefSize = calcPreferredSize();
    m_cellSize = getGridCellSize();
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
  }

  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public void invalidateLayout(Container target) {
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return Component.CENTER_ALIGNMENT;
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return Component.CENTER_ALIGNMENT;
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(m_prefSize);
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(m_prefSize);
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(m_prefSize);
  }

  @Override
  public void layoutContainer(Container parent) {
    int x = 0;
    int y = 0;
    int w = m_prefSize.width;
    int cellW = m_cellSize.width;
    int cellH = m_cellSize.height;

    JPanel headerPanel = m_dateChooser.getHeaderPanel();
    headerPanel.setBounds(x, y, w, cellH);
    y += cellH;

    JPanel daysPanel = m_dateChooser.getDaysPanel();
    daysPanel.setBounds(x, y, cellW * 7, cellH);
    y += cellH;

    m_dateChooser.getCellsPanel().setBounds(x, y, w, 6 * cellH);
  }

  private Dimension calcPreferredSize() {
    Dimension gridCellSize = getGridCellSize();
    int numRows = 8;
    int numHorizontalLines = numRows - 1;
    int columns = 7;
    int height = numRows * gridCellSize.height + numHorizontalLines;
    int width = columns * gridCellSize.width;
    return new Dimension(width, height);
  }

  private Dimension getGridCellSize() {
    Dimension dim = UIManager.getDimension("Calendar.gridCellSize");
    if (dim == null) {
      dim = new Dimension(23, 23);
    }
    return dim;
  }

}
