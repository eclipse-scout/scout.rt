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
package org.eclipse.scout.rt.ui.swing.ext.activitymap;

import java.util.TreeSet;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.NumberUtility;

public class ActivityMapSelection {

  // current selection
  private TreeSet<Integer> m_rows = new TreeSet<Integer>();
  private double[] m_range;

  // temporary selections
  private boolean m_extend = true;// false=reduce
  // anchor
  private boolean m_hasAnchor;
  private int m_anchorRow;
  private double[] m_anchorRange;
  // lead
  private boolean m_hasLead;
  private int m_leadRow;
  private double[] m_leadRange;

  public ActivityMapSelection() {
    clear();
  }

  public ActivityMapSelection(ActivityMapSelection s) {
    m_rows = new TreeSet<Integer>(s.m_rows);
    m_range = s.m_range;
    m_hasAnchor = s.m_hasAnchor;
    m_anchorRow = s.m_anchorRow;
    if (s.m_anchorRange != null) {
      m_anchorRange = new double[]{s.m_anchorRange[0], s.m_anchorRange[1]};
    }
    m_hasLead = s.m_hasLead;
    m_leadRow = s.m_leadRow;
    if (s.m_leadRange != null) {
      m_leadRange = new double[]{s.m_leadRange[0], s.m_leadRange[1]};
    }
    m_extend = s.m_extend;
  }

  /**
   * @return the normalized range [begin,end] in the range 0..1,0..1
   */
  public double[] getRange() {
    if (m_hasAnchor && m_hasLead && m_extend) {
      if (m_range != null) {
        return new double[]{
            NumberUtility.min(m_range[0], m_anchorRange[0], m_leadRange[0]),
            NumberUtility.max(m_range[1], m_anchorRange[1], m_leadRange[1])};
      }
      else {
        return new double[]{
            NumberUtility.min(m_anchorRange[0], m_leadRange[0]),
            NumberUtility.max(m_anchorRange[1], m_leadRange[1])};
      }
    }
    else {
      return m_range;
    }
  }

  /**
   * set the normalized range [begin,end] in the range 0..1,0..1
   */
  public void setRange(double[] newRange) {
    if (newRange != null && newRange[1] - newRange[0] < 0) {
      newRange = new double[]{newRange[1], newRange[0]};
    }
    if (!CompareUtility.equals(m_range, newRange)) {
      m_range = newRange;
    }
  }

  public int[] getRows() {
    TreeSet<Integer> set = new TreeSet<Integer>(m_rows);
    if (m_hasAnchor && m_hasLead) {
      int a = Math.min(m_anchorRow, m_leadRow);
      int b = Math.max(m_anchorRow, m_leadRow);
      for (int i = a; i <= b; i++) {
        if (m_extend) {
          set.add(i);
        }
        else {
          set.remove(i);
        }
      }
    }
    int[] r = new int[set.size()];
    int i = 0;
    for (Integer n : set) {
      r[i++] = n;
    }
    return r;
  }

  /**
   * set the rows
   */
  public void setRows(int[] newRows) {
    m_rows = new TreeSet<Integer>();
    if (newRows != null) {
      for (int i = 0; i < newRows.length; i++) {
        m_rows.add(newRows[i]);
      }
    }
  }

  public void clear() {
    m_rows = new TreeSet<Integer>();
    m_range = null;
    m_hasAnchor = false;
    m_anchorRow = -1;
    m_anchorRange = null;
    m_hasLead = false;
    m_leadRow = -1;
    m_leadRange = null;
  }

  public boolean hasAnchor() {
    return m_hasAnchor;
  }

  public boolean hasLead() {
    return m_hasLead;
  }

  public void setAnchor(int row, double[] range) {
    if (row >= 0) {
      m_extend = true;
      if (m_rows.contains(row)) {
        if (intersects(m_range, range)) {
          m_extend = false;
        }
      }
      m_hasAnchor = true;
      m_anchorRow = row;
      m_anchorRange = range;
    }
  }

  public void clearAnchor() {
    m_hasAnchor = false;
  }

  public void setLead(int row, double[] range) {
    if (row >= 0) {
      m_hasLead = true;
      m_leadRow = row;
      m_leadRange = range;
    }
  }

  public void clearLead() {
    m_hasLead = false;
  }

  /**
   * consume anchor-to-lead and set new anchor to lead, new lead to null
   */
  public void consumeAnchorLead() {
    if (m_hasAnchor && m_hasLead) {
      setRows(getRows());
      setRange(getRange());
    }
    if (m_hasLead) {
      m_hasAnchor = m_hasLead;
      m_anchorRow = m_leadRow;
      m_anchorRange = m_leadRange;
      m_hasLead = false;
      m_leadRow = -1;
      m_leadRange = null;
    }
  }

  public boolean contains(int row, double[] range) {
    if (intersects(getRange(), range)) {
      for (int r : getRows()) {
        if (r == row) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean intersects(double[] a, double[] b) {
    if (a == null || b == null) {
      return false;
    }
    double start = Math.max(a[0], b[0]);
    double end = Math.min(a[1], b[1]);
    return start < end;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ActivityMapSelection) {
      ActivityMapSelection o = (ActivityMapSelection) obj;
      return CompareUtility.equals(this.m_rows, o.m_rows) &&
          CompareUtility.equals(this.m_range, o.m_range) &&
          this.m_hasAnchor == o.m_hasAnchor &&
          CompareUtility.equals(this.m_anchorRange, o.m_anchorRange) &&
          this.m_anchorRow == o.m_anchorRow &&
          this.m_hasLead == o.m_hasAnchor &&
          CompareUtility.equals(this.m_leadRange, o.m_leadRange) &&
          this.m_leadRow == o.m_leadRow;
    }
    return false;
  }
}
