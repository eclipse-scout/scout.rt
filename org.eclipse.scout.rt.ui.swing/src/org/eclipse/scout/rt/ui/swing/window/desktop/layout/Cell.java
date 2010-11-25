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
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Only works together with the PerspectiveDesktopManager to support
 * Eclipse-like JInternalFrame handling inside JDesktopPane The constraint is a
 * 3x3 priority matrix for every 3x3 cell
 */
class Cell {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Cell.class);

  private int m_row;
  private int m_col;
  private boolean m_multiView;
  // views contained in this cell, including distribution map
  private ArrayList<CellElement> m_elements = new ArrayList<CellElement>();

  public Cell(int row, int col, boolean multiView) {
    m_row = row;
    m_col = col;
    m_multiView = multiView;
  }

  public int getRow() {
    return m_row;
  }

  public int getColumn() {
    return m_col;
  }

  public boolean isMultiView() {
    return m_multiView;
  }

  public void setMultiView(boolean b) {
    m_multiView = b;
  }

  public int getCellElementCount() {
    return m_elements.size();
  }

  public CellElement getCellElement(int index) {
    return m_elements.get(index);
  }

  public void setView(Component c, float[][] distributionMap3x3) {
    m_elements.clear();
    addView(c, distributionMap3x3);
  }

  public void addView(Component c, float[][] distributionMap3x3) {
    for (Iterator<CellElement> it = m_elements.iterator(); it.hasNext();) {
      CellElement v = it.next();
      if (v.getComponent() == c) {
        // already added
        return;
      }
    }
    //
    if (m_multiView) {
      m_elements.add(new CellElement(c, distributionMap3x3));
    }
    else {
      m_elements.clear();
      m_elements.add(new CellElement(c, distributionMap3x3));
    }
  }

  public void removeView(Component c) {
    for (Iterator<CellElement> it = m_elements.iterator(); it.hasNext();) {
      CellElement v = it.next();
      if (v.getComponent() == c) {
        it.remove();
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_row + ", " + m_col + ", elements=" + VerboseUtility.dumpObject(m_elements) + "]";
  }

}
