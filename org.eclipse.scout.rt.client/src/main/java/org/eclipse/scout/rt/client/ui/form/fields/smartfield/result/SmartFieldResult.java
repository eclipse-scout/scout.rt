package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartFieldResult<LOOKUP_KEY> implements ISmartFieldResult<LOOKUP_KEY> {

  private List<ILookupRow<LOOKUP_KEY>> m_lookupRows;
  private IQueryParam m_queryParam;
  private Throwable m_exception;

  public SmartFieldResult() {
  }

  public SmartFieldResult(List<ILookupRow<LOOKUP_KEY>> lookupRows, IQueryParam queryParam, Throwable exception) {
    m_lookupRows = lookupRows;
    m_queryParam = queryParam;
    m_exception = exception;
  }

  @Override
  public IQueryParam getQueryParam() {
    return m_queryParam;
  }

  @Override
  public Throwable getException() {
    return m_exception;
  }

  @Override
  public List<ILookupRow<LOOKUP_KEY>> getLookupRows() {
    return m_lookupRows;
  }

}
