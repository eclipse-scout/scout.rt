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
import java.awt.Insets;
import java.awt.LayoutManager2;

import javax.swing.JPanel;
import javax.swing.UIManager;

public class LargeCalendarLayout implements LayoutManager2 {
  private DateChooser m_dateChooser;
  private Dimension m_minDim, m_maxDim, m_prefDim;

  public LargeCalendarLayout(DateChooser dateChooser) {
    m_dateChooser = dateChooser;
    m_prefDim = UIManager.getDimension("Calendar.large.preferredSize");
    if (m_prefDim == null) {
      m_prefDim = new Dimension(700, 500);
    }
    m_minDim = new Dimension(180, 140);
    m_maxDim = new Dimension(4000, 3000);
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
    return new Dimension(m_minDim);
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(m_maxDim);
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(m_prefDim);
  }

  @Override
  public void layoutContainer(Container parent) {
    JPanel headerPanel = m_dateChooser.getHeaderPanel();
    JPanel daysPanel = m_dateChooser.getDaysPanel();
    JPanel controlPanel = m_dateChooser.getControlPanel();
    //
    Dimension size = parent.getSize();
    Insets insets = parent.getInsets();
    if (insets == null) insets = new Insets(0, 0, 0, 0);
    int x = insets.left;
    int y = insets.top;
    int w = size.width - insets.left - insets.right;
    int h = size.height - insets.top - insets.bottom;

    if (headerPanel != null && headerPanel.isVisible()) {
      int headerHeight = headerPanel.getPreferredSize().height;
      headerPanel.setBounds(x, y, w, headerHeight);
      y = y + headerHeight;
    }

    // days panel
    if (daysPanel.isVisible()) {
      int dayH = daysPanel.getComponent(0).getPreferredSize().height;
      daysPanel.setBounds(x, y, w, dayH);
      y = y + dayH;
    }

    int controlHeight = 0;
    if (controlPanel != null && controlPanel.isVisible()) {
      controlHeight = controlPanel.getPreferredSize().height;
    }

    // cells
    JPanel cellsPanel = m_dateChooser.getCellsPanel();
    int yNext = h - controlHeight;
    cellsPanel.setBounds(x, y, w, yNext - y);
    y = yNext;

    if (controlPanel != null && controlPanel.isVisible()) {
      controlPanel.setBounds(x, y, w, controlHeight);
      y = y + controlHeight;
    }

    // calculate timeless section height hint
    m_dateChooser.updateTimelessSectionHeightHint();
  }
}
