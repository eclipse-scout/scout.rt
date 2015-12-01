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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * @since 3.9.0
 */
public abstract class AbstractPagingSupport {

  private int m_pageSize;
  private int m_pageIndex;
  private int m_currentPageStartRowIndex;
  private IMobileTable m_table;
  private ITableRow m_nextElementsTableRow;
  private ITableRow m_previousElementsTableRow;

  public AbstractPagingSupport(IMobileTable table) {
    m_table = table;
    initProperties();
  }

  protected void initProperties() {
    setPageSize(m_table.getPageSize());
    setPageIndex(m_table.getPageIndex());
  }

  public List<ITableRow> getElementsOfCurrentPage(List<? extends ITableRow> rows) {
    initProperties();
    if (m_pageSize <= 0 || m_currentPageStartRowIndex < 0 || m_currentPageStartRowIndex >= rows.size()) {
      return CollectionUtility.arrayList(rows);
    }
    if (m_currentPageStartRowIndex == 0 && rows.size() < m_pageSize) {
      return CollectionUtility.arrayList(rows);
    }

    int currentPageEndRowIndex = Math.min(m_currentPageStartRowIndex + m_pageSize, rows.size()) - 1;
    int currentPageSize = currentPageEndRowIndex - m_currentPageStartRowIndex + 1;
    List<ITableRow> currentPage = new ArrayList<ITableRow>();
    for (int i = m_currentPageStartRowIndex; i <= currentPageEndRowIndex; i++) {
      currentPage.add(rows.get(i));
    }

    if (m_currentPageStartRowIndex > 0) {
      m_previousElementsTableRow = createPreviousElementsTableRow();
      currentPage.add(0, m_previousElementsTableRow);
    }

    if (currentPageSize >= m_pageSize && m_currentPageStartRowIndex + m_pageSize < rows.size()) {
      m_nextElementsTableRow = createNextElementsTableRow();
      currentPage.add(m_nextElementsTableRow);
    }

    return currentPage;
  }

  protected ITableRow createPreviousElementsTableRow() {
    return new PagingTableRow(m_table.getColumnSet(), PagingTableRow.Type.back);
  }

  protected ITableRow createNextElementsTableRow() {
    return new PagingTableRow(m_table.getColumnSet(), PagingTableRow.Type.forward);
  }

  public int getCurrentPageStartRowIndex() {
    return m_currentPageStartRowIndex;
  }

  public int getPageIndex() {
    return m_pageIndex;
  }

  protected void setPageSize(int pageSize) {
    m_pageSize = pageSize;
    m_currentPageStartRowIndex = m_pageIndex * m_pageSize;
  }

  protected void setPageIndex(int pageIndex) {
    m_pageIndex = pageIndex;
    m_currentPageStartRowIndex = m_pageIndex * m_pageSize;
  }

  public int getPageSize() {
    return m_pageSize;
  }

  public ITableRow getPreviousElementsTableRow() {
    return m_previousElementsTableRow;
  }

  public ITableRow getNextElementsTableRow() {
    return m_nextElementsTableRow;
  }

  public IMobileTable getTable() {
    return m_table;
  }
}
