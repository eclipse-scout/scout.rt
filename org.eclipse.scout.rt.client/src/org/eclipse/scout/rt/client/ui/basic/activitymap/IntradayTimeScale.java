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

import java.util.Date;

public class IntradayTimeScale extends TimeScale {

  @Override
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
        // special handling: the minCols might contain a gap, i.e. the range is not continuous.
        // In case, the start date is not in the range of the column, ascribe the date to the next column.
        // Unlike in the super class TimeScale the comparator >= is used here because the end boundaries of the columns contain the exact values.
        // (i.e. minCols[i].getEndTime() is exactly the same as minCols[i+1].getStartTime())
        if (startTime.compareTo(minCols[i].getEndTime()) >= 0) {
          return i < minCols.length - 1 ? i + 1 : null;
        }
        return i;
      }
    }
    return null;
  }

  @Override
  protected Integer getEndMinorColumnIndex(Date endTime) {
    if (endTime == null) {
      return null;
    }
    if (endTime.before(getBeginTime())) {
      return null;
    }
    MinorTimeColumn[] minCols = getMinorTimeColumns();
    if (endTime.after(getEndTime())) {
      return getMinorTimeColumns().length - 1;
    }
    //approach in ascending order
    for (int i = 0; i < minCols.length; i++) {
      if (endTime.compareTo(minCols[i].getEndTime()) <= 0) {
        // special handling: the minCols might contain a gap, i.e. the range is not continuous.
        // In that case, check whether the end date is really in that range. Otherwise, ascribe the date to the previous column.
        // In this case the endTime has to be compared to the beginTime of the column and if the endTime is smaller or equal than
        // the beginTime, the previous column is returned. Unlike in the super class TimeScale the comparator <= is used here
        // because the end boundaries of the columns contain the exact values.
        // (i.e. minCols[i].getEndTime() is exactly the same as minCols[i+1].getStartTime())
        if (endTime.compareTo(minCols[i].getBeginTime()) <= 0) {
          return i > 0 ? i - 1 : null;
        }
        return i;
      }
    }
    return null;
  }
}
