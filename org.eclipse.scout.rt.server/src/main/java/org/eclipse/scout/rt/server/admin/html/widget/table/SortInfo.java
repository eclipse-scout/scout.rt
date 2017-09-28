/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
