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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public class TextColumnUserFilterState extends ColumnUserFilterState {
  private static final long serialVersionUID = 1L;

  private String m_freeText;

  public TextColumnUserFilterState(IColumn<?> column) {
    super(column);
  }

  public String getFreeText() {
    return m_freeText;
  }

  public void setFreeText(String freeText) {
    m_freeText = freeText;
  }
}
