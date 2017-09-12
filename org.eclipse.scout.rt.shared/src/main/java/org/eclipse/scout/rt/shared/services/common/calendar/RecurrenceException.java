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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("squid:S2166")
public class RecurrenceException implements Serializable {
  private static final long serialVersionUID = 1L;
  //
  private ICalendarItem m_item;
  private Date m_originalStartDate;
  private boolean m_exists = true;

  public ICalendarItem getItem() {
    return m_item;
  }

  public void setItem(ICalendarItem i) {
    m_item = i;
  }

  public Date getOriginalStartDate() {
    return m_originalStartDate;
  }

  public void setOriginalStartDate(Date d) {
    m_originalStartDate = d;
  }

  public boolean exists() {
    return m_exists;
  }

  /**
   * mark item as deleted <br>
   * Note: this will not physically delete the item, just set the marker
   */
  public void delete() {
    m_exists = false;
  }
}
