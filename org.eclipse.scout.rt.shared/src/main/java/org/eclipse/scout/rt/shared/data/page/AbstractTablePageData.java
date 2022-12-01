/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.data.page;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Table-based page data. This class extends {@link AbstractTableFieldBeanData} and uses {@link AbstractTableRowData}
 * for holding the contents of a table page.
 *
 * @see AbstractTableFieldBeanData
 * @since 3.10.0-M1
 */
@Bean
public abstract class AbstractTablePageData extends AbstractTableFieldBeanData {
  private static final long serialVersionUID = 1L;

  private boolean m_limitedResult;
  private long m_estimatedRowCount;
  private int m_maxRowCount;

  /**
   * Optional property may be used by the data provider to signal, that the data returned by this instance has been
   * limited.
   *
   * @return Returns <code>true</code> if the rows of this table page data contain only a subset of the records
   *         available in the data source. Otherwise <code>false</code>.
   */
  public boolean isLimitedResult() {
    return m_limitedResult;
  }

  /**
   * Sets whether the data in this bean has been limited (i.e. there exist more records in the data source).
   */
  public void setLimitedResult(boolean limitedResult) {
    m_limitedResult = limitedResult;
  }

  /**
   * Optional property may be used by the data provider to report the estimated available row count in case data in this
   * bean has been limited.
   *
   * @return an estimation of the total available row count, 0 by default (not set)
   * @since 9.0
   */
  public long getEstimatedRowCount() {
    return m_estimatedRowCount;
  }

  /**
   * Optional property may be used by the data provider to report the estimated available row count in case data in this
   * bean has been limited.
   *
   * @param estimatedRowCount
   *          an estimation of the total available row count, 0 by default (not set)
   * @since 9.0
   */
  public void setEstimatedRowCount(long estimatedRowCount) {
    m_estimatedRowCount = estimatedRowCount;
  }

  /**
   * Maximum rows the user is allowed to load into this table, maximal capacity, optional.
   *
   * @since 9.0
   */
  public int getMaxRowCount() {
    return m_maxRowCount;
  }

  /**
   * Maximum rows the user is allowed to load into this table, maximal capacity, optional.
   *
   * @since 9.0
   */
  public void setMaxRowCount(int maxRowCount) {
    m_maxRowCount = maxRowCount;
  }
}
