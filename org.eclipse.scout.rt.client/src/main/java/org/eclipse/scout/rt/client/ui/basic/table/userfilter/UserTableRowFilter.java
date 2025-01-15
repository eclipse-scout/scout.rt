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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilter;

/**
 * @since 5.1
 */
public class UserTableRowFilter implements ITableRowFilter, IUserFilter {
  private final Set<? extends ITableRow> m_rows;

  public UserTableRowFilter(Collection<? extends ITableRow> rows) {
    m_rows = new HashSet<ITableRow>(rows);
  }

  @Override
  public boolean accept(ITableRow row) {
    return m_rows.contains(row);
  }
}
