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
package org.eclipse.scout.rt.ui.html.json.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

public class TableEventFilterCondition {
  private static final long serialVersionUID = 1L;

  private int m_type;
  private List<? extends ITableRow> m_rows;
  private boolean m_checkRows;

  public TableEventFilterCondition(int type) {
    this(type, new ArrayList<ITableRow>());
    m_checkRows = false;
  }

  public TableEventFilterCondition(int type, List<? extends ITableRow> rows) {
    m_rows = CollectionUtility.arrayList(rows);
    m_type = type;
    m_checkRows = true;
  }

  public int getType() {
    return m_type;
  }

  public List<ITableRow> getRows() {
    return CollectionUtility.arrayList(m_rows);
  }

  public boolean checkRows() {
    return m_checkRows;
  }
}
