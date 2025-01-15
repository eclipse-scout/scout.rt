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

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class NumberColumnUserFilterState extends ColumnUserFilterState {
  private static final long serialVersionUID = 1L;

  private BigDecimal m_numberFrom;
  private BigDecimal m_numberTo;

  public NumberColumnUserFilterState(IColumn<?> column) {
    super(column);
  }

  public BigDecimal getNumberFrom() {
    return m_numberFrom;
  }

  public void setNumberFrom(BigDecimal numberFrom) {
    m_numberFrom = numberFrom;
  }

  public BigDecimal getNumberTo() {
    return m_numberTo;
  }

  public void setNumberTo(BigDecimal numberTo) {
    m_numberTo = numberTo;
  }
}
