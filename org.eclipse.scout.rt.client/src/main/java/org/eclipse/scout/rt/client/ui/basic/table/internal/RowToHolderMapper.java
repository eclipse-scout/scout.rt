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
package org.eclipse.scout.rt.client.ui.basic.table.internal;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.holders.IHolder;

public class RowToHolderMapper<T> {
  private ITableRow m_row;
  private IColumn<T> m_col;
  private IHolder<T> m_holder;

  public RowToHolderMapper(ITableRow row, IColumn<T> col, IHolder<T> holder) {
    m_row = row;
    m_col = col;
    m_holder = holder;
  }

  public void exportRowValue() {
    m_holder.setValue(m_col.getValue(m_row));
  }
}
