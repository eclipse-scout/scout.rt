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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldDataFetchResult;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartField2Result<V> {

  private List<ILookupRow<V>> m_lookupRows;
  private boolean m_lookupFailed;
  private String m_searchText;
  private String m_wildcard;

  /**
   * @param result
   */
  public SmartField2Result(IContentAssistFieldDataFetchResult<V> result) {
    m_lookupRows = result.getLookupRows();
    m_lookupFailed = result.getException() != null;
    m_searchText = result.getSearchParam().getSearchText();
    m_wildcard = result.getSearchParam().getWildcard();
  }

  public List<ILookupRow<V>> getLookupRows() {
    return m_lookupRows;
  }

  public boolean isLookupFailed() {
    return m_lookupFailed;
  }

  public String getSearchText() {
    return m_searchText;
  }

  /**
   * @return When no lookup rows have been found and we've searched exactly for the wildcard character (default: *) this
   *         means no data is available (e.g. the database table is empty).
   */
  public boolean isNoData() {
    if (m_lookupRows.size() > 0) {
      return false;
    }
    return ObjectUtility.equals(m_searchText, m_wildcard);
  }

}
