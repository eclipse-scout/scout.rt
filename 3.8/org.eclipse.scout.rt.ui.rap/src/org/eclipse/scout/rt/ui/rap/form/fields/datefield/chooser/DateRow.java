/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.DateUtility;

public class DateRow {

  private Date m_startDate;

  public DateRow(Date date) {
    m_startDate = date;
  }

  public int getIndex(Date date) {
    for (int i = 0; i < 8; i++) {
      if (DateUtility.isSameDay(date, DateUtility.addDays(m_startDate, i))) {
        return i;
      }
    }
    return -1;
  }

  public Date getDate(int offset) {
    Calendar c = Calendar.getInstance();
    c.setTime(m_startDate);
    c.add(Calendar.DATE, offset);
    return c.getTime();
  }
}
