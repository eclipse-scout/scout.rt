/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class DateColumnUserFilterState extends ColumnUserFilterState {
  private static final long serialVersionUID = 1L;

  private Date m_dateFrom;
  private Date m_dateTo;

  public DateColumnUserFilterState(IColumn<?> column) {
    super(column);
  }

  public Date getDateFrom() {
    return m_dateFrom;
  }

  public void setDateFrom(Date dateFrom) {
    m_dateFrom = dateFrom;
  }

  public Date getDateTo() {
    return m_dateTo;
  }

  public void setDateTo(Date dateTo) {
    m_dateTo = dateTo;
  }
}
