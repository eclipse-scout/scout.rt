/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;

/**
 * Description: Allows to store multiple time ranges. There are no overlapping on the stored ranges.
 * <p>
 * e.g. {'1.1.2006 13:00'-'1.1.2006 14:00', '2.1.2006 12:00'-'3.1.2006 12:00'}
 */
class TimeRange {

  private Date m_from;
  private Date m_to;

  public TimeRange(Date from, Date to) {
    m_from = from;
    m_to = to;
  }

  public boolean contains(Date representedDate) {
    return !(representedDate.after(m_to) || representedDate.before(m_from));
  }

  public Date getFrom() {
    return m_from;
  }

  public Date getTo() {
    return m_to;
  }

  public long getDurationMillis() {
    return m_to.getTime() - m_from.getTime();
  }

  public int getDurationMinutes() {
    return (int) (getDurationMillis() / 60000L);
  }
}
