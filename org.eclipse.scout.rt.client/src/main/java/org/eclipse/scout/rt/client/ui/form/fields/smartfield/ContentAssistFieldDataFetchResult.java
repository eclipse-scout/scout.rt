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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class ContentAssistFieldDataFetchResult<LOOKUP_KEY> implements IContentAssistFieldDataFetchResult<LOOKUP_KEY> {

  private final IContentAssistSearchParam<LOOKUP_KEY> m_searchParam;
  private final List<ILookupRow<LOOKUP_KEY>> m_lookupRows;
  private final Throwable m_exception;

  public ContentAssistFieldDataFetchResult(List<ILookupRow<LOOKUP_KEY>> rows, Throwable exception, IContentAssistSearchParam<LOOKUP_KEY> searchParam) {
    if (rows == null) {
      rows = Collections.emptyList();
    }
    m_lookupRows = rows;
    m_exception = exception;
    m_searchParam = searchParam;
  }

  @Override
  public List<ILookupRow<LOOKUP_KEY>> getLookupRows() {
    return m_lookupRows;
  }

  @Override
  public Throwable getException() {
    return m_exception;
  }

  @Override
  public IContentAssistSearchParam<LOOKUP_KEY> getSearchParam() {
    return m_searchParam;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_searchParam == null) ? 0 : m_searchParam.hashCode());
    result = prime * result + ((m_lookupRows == null) ? 0 : m_lookupRows.hashCode());
    result = prime * result + ((m_exception == null) ? 0 : m_exception.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ContentAssistFieldDataFetchResult other = (ContentAssistFieldDataFetchResult) obj;
    if (m_searchParam == null) {
      if (other.m_searchParam != null) {
        return false;
      }
    }
    else if (!m_searchParam.equals(other.m_searchParam)) {
      return false;
    }
    if (m_lookupRows == null) {
      if (other.m_lookupRows != null) {
        return false;
      }
    }
    else if (!m_lookupRows.equals(other.m_lookupRows)) {
      return false;
    }
    if (m_exception == null) {
      if (other.m_exception != null) {
        return false;
      }
    }
    else if (!m_exception.equals(other.m_exception)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr(m_searchParam)
        .attr("lookupRows", m_lookupRows)
        .attr("exception", m_exception)
        .toString();
  }

}
