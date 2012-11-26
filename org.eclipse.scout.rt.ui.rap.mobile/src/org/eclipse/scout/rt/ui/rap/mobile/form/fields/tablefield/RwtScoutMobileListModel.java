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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.scout.rt.client.mobile.ui.basic.table.IMobileTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileListModel extends RwtScoutListModel {

  private static final long serialVersionUID = 1L;

  private RwtPagingSupport m_pagingSupport;

  public RwtScoutMobileListModel(IMobileTable scoutTable, RwtScoutList uiTable) {
    super(scoutTable, uiTable);

    if (scoutTable.isPagingEnabled()) {
      m_pagingSupport = new RwtPagingSupport(uiTable, scoutTable);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    if (m_pagingSupport != null) {
      m_pagingSupport.dispose();
      m_pagingSupport = null;
    }
  }

  @Override
  public IMobileTable getScoutTable() {
    return (IMobileTable) super.getScoutTable();
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_pagingSupport == null) {
      return super.getElements(inputElement);
    }

    if (getScoutTable() != null) {
      ITableRow[] filteredRows = getScoutTable().getFilteredRows();
      return m_pagingSupport.getElementsOfCurrentPage(filteredRows);
    }
    else {
      return new Object[0];
    }
  }

  public void setPagingEnabled(boolean enabled) {
    boolean refreshNecessary = false;
    if (enabled) {
      if (m_pagingSupport == null) {
        m_pagingSupport = new RwtPagingSupport(getUiList(), getScoutTable());
        refreshNecessary = true;
      }
    }
    else {
      if (m_pagingSupport != null) {
        m_pagingSupport.dispose();
        m_pagingSupport = null;
        refreshNecessary = true;
      }
    }

    if (refreshNecessary) {
      getUiList().getUiTableViewer().refresh();
    }
  }

}
