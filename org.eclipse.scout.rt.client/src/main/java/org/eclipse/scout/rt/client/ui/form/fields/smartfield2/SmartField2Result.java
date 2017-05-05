package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldDataFetchResult;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartField2Result<V> {

  private List<ILookupRow<V>> m_lookupRows;

  private boolean m_lookupFailed;

  private String m_searchText;

  /**
   * @param result
   */
  public SmartField2Result(IContentAssistFieldDataFetchResult<V> result) {
    m_lookupRows = result.getLookupRows();
    m_lookupFailed = result.getException() != null;
    m_searchText = result.getSearchParam().getSearchText();
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

}
