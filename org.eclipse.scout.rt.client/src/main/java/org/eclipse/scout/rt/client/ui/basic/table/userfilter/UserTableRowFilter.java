/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import java.util.Collection;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;

/**
 * @since 5.1
 */
public class UserTableRowFilter implements ITableRowFilter {
  private Collection<? extends ITableRow> m_rows;

  public UserTableRowFilter(Collection<? extends ITableRow> rows) {
    m_rows = CollectionUtility.arrayList(rows);
  }

  @Override
  public boolean accept(ITableRow row) {
    return m_rows.contains(row);
  }

}
