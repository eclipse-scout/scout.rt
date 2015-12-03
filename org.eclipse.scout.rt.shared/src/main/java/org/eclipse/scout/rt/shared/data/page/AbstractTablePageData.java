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
package org.eclipse.scout.rt.shared.data.page;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * Table-based page data. This class extends {@link AbstractTableFieldBeanData} and uses {@link AbstractTableRowData}
 * for holding the contents of a table page.
 * 
 * @see AbstractTableFieldBeanData
 * @since 3.10.0-M1
 */
public abstract class AbstractTablePageData extends AbstractTableFieldBeanData {

  private static final long serialVersionUID = 1L;

  private boolean m_limitedResult;

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
   * 
   * @param limitedResult
   */
  public void setLimitedResult(boolean limitedResult) {
    m_limitedResult = limitedResult;
  }
}
