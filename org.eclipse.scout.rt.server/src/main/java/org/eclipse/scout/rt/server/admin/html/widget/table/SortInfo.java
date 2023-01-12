/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.html.widget.table;

public class SortInfo {

  private int m_columnIndex = -1;
  private boolean m_ascending = true;

  public int getColumnIndex() {
    return m_columnIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.m_columnIndex = columnIndex;
  }

  public boolean isAscending() {
    return m_ascending;
  }

  public void setAscending(boolean ascending) {
    this.m_ascending = ascending;
  }
}
