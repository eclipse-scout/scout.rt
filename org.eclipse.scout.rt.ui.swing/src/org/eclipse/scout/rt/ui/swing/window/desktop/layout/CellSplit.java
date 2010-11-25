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

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

class CellSplit {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CellSplit.class);

  private boolean m_rowSplit;
  private int m_row;
  private int m_col;
  private Cell m_leftItem;
  private Cell m_rightItem;
  private boolean m_fixed;
  private int m_location;
  private IMultiSplitStrategy m_strategy;
  /*
   * ticket 90942: don't validate size on unused/invisible splits
   */
  private boolean m_usedInModel;

  public CellSplit(boolean rowSplit, int row, int col, boolean fixed, Cell leftItem, Cell rightItem, int location) {
    this(rowSplit, row, col, fixed, leftItem, rightItem, location, null);
  }

  public CellSplit(boolean rowSplit, int row, int col, boolean fixed, Cell leftItem, Cell rightItem, int location, IMultiSplitStrategy strategy) {
    m_fixed = fixed;
    m_row = row;
    m_col = col;
    m_rowSplit = rowSplit;
    m_leftItem = leftItem;
    m_rightItem = rightItem;
    m_location = location;
    m_strategy = strategy;
  }

  public int getLocation() {
    return m_strategy != null ? m_strategy.getSplitLocation(m_row, m_col) : m_location;
  }

  public void setLocation(int loc) {
    m_location = loc;
    if (m_strategy != null) {
      m_strategy.setSplitLocation(m_row, m_col, loc);
    }
  }

  public boolean isFixed() {
    return m_fixed;
  }

  public boolean isRowSplit() {
    return m_rowSplit;
  }

  public boolean isColumnSplit() {
    return !m_rowSplit;
  }

  public Cell getLeftItem() {
    return m_leftItem;
  }

  public Cell getRightItem() {
    return m_rightItem;
  }

  public boolean isUsedInModel() {
    return m_usedInModel;
  }

  public void setUsedInModel(boolean usedInModel) {
    m_usedInModel = usedInModel;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "[" + (m_rowSplit ? "rowsplit " : "colsplit ") + (m_fixed ? "fixed " : "") + m_location + " " + VerboseUtility.dumpObject(m_leftItem) + " " + VerboseUtility.dumpObject(m_rightItem) + "]";
  }

}
