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
package org.eclipse.scout.rt.client.ui.basic.activitymap;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;

/**
 * ui model to represent time column model with a major scale and a minor scale
 * (contained in the major scale)
 */
public class TimeScale {
  private DateFormat m_dateFormat;
  private final List<MajorTimeColumn> m_childrenUnsorted = new ArrayList<MajorTimeColumn>(0);
  // cache for sorted objects, only access using the provided methods, never
  // access this member directly
  private MajorTimeColumn[] m_majorColumnsSorted;
  private MinorTimeColumn[] m_minorColumnsSorted;

  public static final int SMALL = 0;
  public static final int MEDIUM = 1;
  public static final int LARGE = 2;

  public TimeScale() {
  }

  protected void addMajorColumnNotify(MajorTimeColumn majorCol) {
    m_childrenUnsorted.add(majorCol);
    // clear cache
    m_majorColumnsSorted = null;
    m_minorColumnsSorted = null;
  }

  protected void addMinorColumnNotify(MajorTimeColumn majorCol, MinorTimeColumn minorCol) {
    // clear cache
    m_majorColumnsSorted = null;
    m_minorColumnsSorted = null;
  }

  public MajorTimeColumn[] getMajorTimeColumns() {
    if (m_majorColumnsSorted == null) {
      TreeMap<CompositeObject, MajorTimeColumn> sortMap = new TreeMap<CompositeObject, MajorTimeColumn>();
      int index = 0;
      for (MajorTimeColumn c : m_childrenUnsorted) {
        MinorTimeColumn[] minorCols = c.getMinorTimeColumns();
        if (minorCols.length > 0) {
          sortMap.put(new CompositeObject(minorCols[0].getBeginTime(), index), c);
        }
        else {
          sortMap.put(new CompositeObject(new Date(), index), c);
        }
        index++;
      }
      m_majorColumnsSorted = sortMap.values().toArray(new MajorTimeColumn[sortMap.size()]);
    }
    return m_majorColumnsSorted;
  }

  public MinorTimeColumn[] getMinorTimeColumns() {
    if (m_minorColumnsSorted == null) {
      TreeMap<Date, MinorTimeColumn> sortMap = new TreeMap<Date, MinorTimeColumn>();
      for (MajorTimeColumn c : getMajorTimeColumns()) {
        for (MinorTimeColumn minorCol : c.getMinorTimeColumns()) {
          sortMap.put(minorCol.getBeginTime(), minorCol);
        }
      }
      m_minorColumnsSorted = sortMap.values().toArray(new MinorTimeColumn[sortMap.size()]);
    }
    return m_minorColumnsSorted;
  }

  public Date getBeginTime() {
    MinorTimeColumn[] cols = getMinorTimeColumns();
    if (cols.length > 0) {
      return cols[0].getBeginTime();
    }
    return new Date();
  }

  public Date getEndTime() {
    MinorTimeColumn[] cols = getMinorTimeColumns();
    if (cols.length > 0) {
      return cols[cols.length - 1].getEndTime();
    }
    return new Date();
  }

  public Calendar getBeginCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(getBeginTime());
    return cal;
  }

  public DateFormat getDateFormat() {
    return m_dateFormat;
  }

  public void setDateFormat(DateFormat s) {
    m_dateFormat = s;
  }

  /**
   * Normalized range [begin,end] of the item The normalized space starts at 0.0
   * and ends at 1.0 Every minor column has the same with in this space
   * 
   * @return float[begin,end] or null when item is not intersecting this time
   *         scale. May return values <0 or >1 when item is reaching outside of
   *         the range
   */
  public double[] getRangeOf(Date beginTime, Date endTime) {
    Integer a = null, b = null;
    a = getStartMinorColumnIndex(beginTime);
    if (a == null) {
      return null;
    }
    if (beginTime.equals(endTime)) {
      b = a;
    }
    if (b == null) {
      b = getEndMinorColumnIndex(endTime);
    }
    if (b == null) {
      return null;
    }
    return new double[]{indexToRange(a)[0], indexToRange(b)[1]};
  }

  protected Integer getStartMinorColumnIndex(Date startTime) {
    if (startTime == null) {
      return null;
    }
    if (startTime.before(getBeginTime())) {
      return 0;
    }
    if (startTime.after(getEndTime())) {
      return null;
    }
    MinorTimeColumn[] minCols = getMinorTimeColumns();
    //approach in descending order
    for (int i = minCols.length - 1; i >= 0; i--) {
      if (startTime.compareTo(minCols[i].getBeginTime()) >= 0) {
        return i;
      }
    }
    return null;
  }

  protected Integer getEndMinorColumnIndex(Date endTime) {
    if (endTime == null) {
      return null;
    }
    if (endTime.before(getBeginTime())) {
      return null;
    }
    if (endTime.after(getEndTime())) {
      return getMinorTimeColumns().length - 1;
    }
    //approach in ascending order
    MinorTimeColumn[] minCols = getMinorTimeColumns();
    for (int i = 0; i < minCols.length; i++) {
      if (endTime.compareTo(minCols[i].getEndTime()) <= 0) {
        // special handling: the minCols might contain a gap, i.e. the range is not contiguous.
        // In that case, check whether the end date is really in that range. Otherwise, ascribe the date to the previous column.
        // In this case the endTime has to be compared to the beginTime of the column and if the endTime is strictly smaller than
        // the beginTime, the previous column is returned. This is needed because 1ms is subtracted from the end boundary during
        // construction of MinorColumn.
        if (endTime.compareTo(minCols[i].getBeginTime()) < 0) {
          return i > 0 ? i - 1 : null;
        }
        return i;
      }
    }
    return null;
  }

  /**
   * Normalized range [begin,end] of the column The normalized space starts at
   * 0.0 and ends at 1.0
   * 
   * @return double[begin,end] or null
   */
  public double[] getRangeOf(MajorTimeColumn column) {
    MinorTimeColumn[] minorCols = column.getMinorTimeColumns();
    if (minorCols.length > 0) {
      double[] r1 = getRangeOf(minorCols[0]);
      double[] r2 = getRangeOf(minorCols[minorCols.length - 1]);
      if (r1 != null && r1.length > 0 && r2 != null && r2.length > 1) {
        return new double[]{r1[0], r2[1]};
      }
    }
    return null;
  }

  /**
   * Normalized location [begin,end] of the column The normalized space starts
   * at 0.0 and ends at 1.0
   * 
   * @return double[begin,end] or null
   */
  public double[] getRangeOf(MinorTimeColumn column) {
    MinorTimeColumn[] minCols = getMinorTimeColumns();
    int count = minCols.length;
    for (int i = 0; i < count; i++) {
      if (minCols[i] == column) {
        return indexToRange(i);
      }
    }
    return null;
  }

  private double[] indexToRange(int index) {
    int count = getMinorTimeColumns().length;
    return new double[]{1.0 * index / count, 1.0 * (index + 1.0) / count};
  }

  private int locationToIndex(double d) {
    int count = getMinorTimeColumns().length;
    if (count == 0) {
      return -1;
    }
    int i = (int) (d * count);
    if (i >= count) {
      i = count - 1;
    }
    else if (i < 0) {
      i = 0;
    }
    return i;
  }

  /**
   * Normalized snap to minor column in normalized space
   * 
   * @return nearest double for input location
   */
  public double[] snapRange(double normalizedLocation) {
    int i = locationToIndex(normalizedLocation);
    if (i >= 0) {
      return indexToRange(i);
    }
    else {
      return new double[]{0, 0};
    }
  }

  /**
   * @return nearest {@link MinorTimeColumn} for normalized location
   */
  public MinorTimeColumn getMinorTimeColumn(double normalizedLocation) {
    MinorTimeColumn[] minorCols = getMinorTimeColumns();
    int i = locationToIndex(normalizedLocation);
    if (i >= 0) {
      return minorCols[i];
    }
    else {
      return null;
    }
  }

  public Date getTimeByLocation(double normalizedLocation) {
    int i = locationToIndex(normalizedLocation);
    if (i >= 0) {
      MinorTimeColumn minorCol = getMinorTimeColumns()[i];
      double[] a = indexToRange(i);
      if (normalizedLocation < a[0]) {
        return minorCol.getBeginTime();
      }
      else if (normalizedLocation <= a[1]) {
        double r = (normalizedLocation - a[0]) / (a[1] - a[0]);
        return new Date((long) ((1 - r) * minorCol.getBeginTime().getTime() + r * minorCol.getEndTime().getTime()));
      }
      else {
        return minorCol.getEndTime();
      }
    }
    else {
      return null;
    }
  }

  @Override
  public String toString() {
    return toString(LARGE);
  }

  public String toString(int size) {
    StringBuilder b = new StringBuilder();
    b.append(getClass().getSimpleName());
    b.append("[");
    for (MajorTimeColumn c : getMajorTimeColumns()) {
      b.append(" ");
      b.append(c.toString(size));
    }
    b.append(" ]");
    return b.toString();
  }
}
