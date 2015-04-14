/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

abstract class AbstractProposalChooser<T, LOOKUP_KEY> extends AbstractPropertyObserver implements IProposalChooser<T, LOOKUP_KEY> {

  protected IContentAssistField<?, LOOKUP_KEY> m_contentAssistField;

  protected T m_model;

  private final boolean m_allowCustomText;

  protected AbstractProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) throws ProcessingException {
    m_contentAssistField = contentAssistField;
    m_allowCustomText = allowCustomText;
    propertySupport.setProperty(PROP_ACTIVE_STATE_FILTER_ENABLED, m_contentAssistField.isActiveFilterEnabled());
    propertySupport.setProperty(PROP_ACTIVE_STATE_FILTER, m_contentAssistField.getActiveFilter());
    m_model = createModel();
    setStatusVisible(true);
    init();
  }

  /**
   * Used to create the 'model' of the proposal chooser. In this method you shouldn't call methods that access the
   * m_model variable since it isn't set until this method has completed. Use the {@link #init()} method instead.
   */
  abstract protected T createModel() throws ProcessingException;

  /**
   * Init method called by the CTOR of the class, after createModel() has been called and m_model variable is set.
   * The default implementation does nothing.
   */
  protected void init() throws ProcessingException {
  }

  @Override
  public ILookupRow<LOOKUP_KEY> getAcceptedProposal() {
    ILookupRow<LOOKUP_KEY> row = getSelectedLookupRow();
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (m_allowCustomText) {
      return null;
    }
    else {
      return execGetSingleMatch();
    }
  }

  protected abstract ILookupRow<LOOKUP_KEY> getSelectedLookupRow();

  protected abstract ILookupRow<LOOKUP_KEY> execGetSingleMatch();

  protected abstract void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount);

  @Override
  public String getSearchText() {
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> searchResult = getSearchResult();
    if (searchResult != null) {
      return searchResult.getSearchText();
    }
    return null;
  }

  private void setSearchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    propertySupport.setProperty(PROP_SEARCH_RESULT, result);
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldDataFetchResult<LOOKUP_KEY> getSearchResult() {
    return (IContentAssistFieldDataFetchResult<LOOKUP_KEY>) propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  @Override
  public void setStatus(IStatus status) {
    propertySupport.setProperty(PROP_STATUS, status);
  }

  @Override
  public IStatus getStatus() {
    return (IStatus) propertySupport.getProperty(PROP_STATUS);
  }

  @Override
  public void setStatusVisible(boolean statusVisible) {
    propertySupport.setProperty(PROP_STATUS_VISIBLE, statusVisible);
  }

  @Override
  public boolean isStatusVisible() {
    return propertySupport.getPropertyBool(PROP_STATUS_VISIBLE);
  }

  @Override
  public T getModel() {
    return m_model;
  }

  @Override
  public final void dataFetchedDelegate(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    dataFetchedDelegateImpl(result, maxCount);
    setSearchResult(result);
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return m_contentAssistField.isActiveFilterEnabled();
  }

  @Override
  public TriState getActiveFilter() {
    return m_contentAssistField.getActiveFilter();
  }

  @Override
  public void updateActiveFilter(TriState activeState) {
    m_contentAssistField.setActiveFilter(activeState);
    m_contentAssistField.doSearch(false, false);
  }

  /**
   * @deprecated Only used for Swing client, remove when Swing client is no more,
   *             before 6.0.0 is shipped.
   */
  @Deprecated
  protected final void fireStructureChanged() {
    propertySupport.firePropertyChange(SWING_STRUCTURE_CHANGED, null, this);
  }

  public final boolean isAllowCustomText() {
    return m_allowCustomText;
  }

}
