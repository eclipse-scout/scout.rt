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
        // special handling: the minCols might contain a gap, i.e. the range is not contiguous.
        // In that case, check whether the end date is really in that range. Otherwise, ascribe the date to the previous column.
        // In this case the endTime has to be compared to the beginTime of the column and if the endTime is smaller or equal than
        // the beginTime, the previous column is returned. Compared to the super class TimeScale the comparator <= is used here
        // because the end boundaries of the columns contain the exact values.
        if (endTime.compareTo(minCols[i].getBeginTime()) <= 0) {
          return i > 0 ? i - 1 : null;
        }
        return i;
      }
    }
    return null;
  }
}
