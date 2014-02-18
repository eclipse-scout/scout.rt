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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 *
 */
public class ContentAssistFieldDataFetchResult<LOOKUP_TYPE> implements IContentAssistFieldDataFetchResult<LOOKUP_TYPE> {

  private final List<? extends ILookupRow<LOOKUP_TYPE>> m_lookupRows;
  private final ProcessingException m_processingException;
  private final String m_searchText;
  private final boolean m_selectCurrentValue;

  public ContentAssistFieldDataFetchResult(List<? extends ILookupRow<LOOKUP_TYPE>> rows, ProcessingException failed, String searchText, boolean selectCurrentValue) {
    m_lookupRows = rows;
    m_processingException = failed;
    m_searchText = searchText;
    m_selectCurrentValue = selectCurrentValue;
  }

  @Override
  public List<? extends ILookupRow<LOOKUP_TYPE>> getLookupRows() {
    return m_lookupRows;
  }

  @Override
  public ProcessingException getProcessingException() {
    return m_processingException;
  }

  @Override
  public String getSearchText() {
    return m_searchText;
  }

  @Override
  public boolean isSelectCurrentValue() {
    return m_selectCurrentValue;
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
    if (m_lookupRows == null) {
      if (other.m_lookupRows != null) {
        return false;
      }
    }
    else if (!m_lookupRows.equals(other.m_lookupRows)) {
      return false;
    }

    if (m_processingException == null && other.getProcessingException() != null) {
      return false;
    }
    else if (m_processingException != null && other.getProcessingException() == null) {
      return false;
    }
    else if (m_processingException != null && other.getProcessingException() != null) {
      IProcessingStatus status = m_processingException.getStatus();
      IProcessingStatus otherStatus = other.getProcessingException().getStatus();
      if (status == null) {
        if (otherStatus != null) {
          return false;
        }
      }
      else if (!status.equals(otherStatus)) {
        return false;
      }
    }
    if (m_searchText == null) {
      if (other.m_searchText != null) {
        return false;
      }
    }
    else if (!m_searchText.equals(other.m_searchText)) {
      return false;
    }
    if (m_selectCurrentValue != other.m_selectCurrentValue) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_lookupRows == null) ? 0 : m_lookupRows.hashCode());
    result = prime * result + ((m_processingException == null || m_processingException.getStatus() == null) ? 0 : m_processingException.getStatus().hashCode());
    result = prime * result + ((m_searchText == null) ? 0 : m_searchText.hashCode());
    result = prime * result + (m_selectCurrentValue ? 1231 : 1237);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LookupRowFetcherResult {");
    builder.append("[searchText:'").append(getSearchText()).append("']");
    builder.append(" [rowCount:'");
    if (getLookupRows() != null) {
      builder.append(getLookupRows().size());
    }
    else {
      builder.append("null");
    }
    builder.append("']");
    builder.append(" [selectCurrentValue:'").append(isSelectCurrentValue()).append("']");
    builder.append(" [processingException:'").append(getProcessingException()).append("']");
    builder.append("}");
    return builder.toString();
  }
}
