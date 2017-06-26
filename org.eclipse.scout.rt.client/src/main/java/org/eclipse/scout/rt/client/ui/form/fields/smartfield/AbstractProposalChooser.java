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

import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractProposalChooser<T, LOOKUP_KEY> extends AbstractPropertyObserver implements IProposalChooser<T, LOOKUP_KEY> {

  protected IContentAssistField<?, LOOKUP_KEY> m_contentAssistField;

  protected T m_model;

  private final boolean m_allowCustomText;

  protected AbstractProposalChooser(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) {
    m_contentAssistField = contentAssistField;
    m_allowCustomText = allowCustomText;
    propertySupport.setProperty(PROP_ACTIVE_STATE_FILTER_ENABLED, m_contentAssistField.isActiveFilterEnabled());
    propertySupport.setProperty(PROP_ACTIVE_STATE_FILTER, m_contentAssistField.getActiveFilter());
    m_model = createModel();
    setStatusVisible(true);
    init();
  }

  /**
   * Creates a new instance of the proposal model if the smart-field has an inner class for tree or table, or returns an
   * instance of the default class for the proposal model.
   */
  protected T createConfiguredOrDefaultModel(Class<?> modelInterface) {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(m_contentAssistField.getClass());
    // We cannot use 'Class<T> modelInterface' since we cannot pass a parameterized class like ArrayList<String> to it
    @SuppressWarnings("unchecked")
    Class<T> modelClass = ConfigurationUtility.filterClass(dca, (Class<T>) modelInterface);
    if (modelClass == null) {
      return createDefaultModel();
    }
    return ConfigurationUtility.newInnerInstance(m_contentAssistField, modelClass);
  }

  /**
   * Used to create the 'model' of the proposal chooser. In this method you shouldn't call methods that access the
   * m_model variable since it isn't set until this method has completed. Use the {@link #init()} method instead.
   */
  protected abstract T createModel();

  /**
   * Called when smart-field doesn't provide a inner class for a proposal model (tree or table). Returns the default
   * proposal model.
   */
  protected abstract T createDefaultModel();

  /**
   * Init method called by the CTOR of the class, after createModel() has been called and m_model variable is set. The
   * default implementation calls {@link AbstractProposalChooser#initModel()}.
   */
  protected void init() {
    initModel();
  }

  /**
   * Init the model (tree or table).
   */
  protected abstract void initModel();

  @Override
  public ILookupRow<LOOKUP_KEY> getAcceptedProposal() {
    ILookupRow<LOOKUP_KEY> row = getSelectedLookupRow();
    if (row != null && row.isEnabled()) {
      return row;
    }
    else if (m_allowCustomText || isBrowseAll()) {
      return null;
    }
    else {
      return execGetSingleMatch();
    }
  }

  protected abstract ILookupRow<LOOKUP_KEY> getSelectedLookupRow();

  protected abstract ILookupRow<LOOKUP_KEY> execGetSingleMatch();

  protected abstract void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount);

  /**
   * Returns true when search text is "browse all" (*). This is required to avoid a problem where we have a lookup call
   * that returns only a single row. If execSingleMatch() would apply in that case it wouldn't be possible to set the
   * field to null, because the single match is always selected automatically.
   *
   * @return
   */
  protected boolean isBrowseAll() {
    return m_contentAssistField.getWildcard().equals(getSearchText());
  }

  @Override
  public String getSearchText() {
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> searchResult = getSearchResult();
    if (searchResult != null) {
      return searchResult.getSearchParam().getSearchQuery();
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

  /**
   * Note: currently a SmartField with a tree does not show a status when no proposals have been found. That's
   * inconsistent with SmartFields with a table. They do show a status in that case, saying 'no proposals have been
   * found'.
   */
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
  public String[] getActiveFilterLabels() {
    return m_contentAssistField.getActiveFilterLabels();
  }

  @Override
  public void updateActiveFilter(TriState activeState) {
    m_contentAssistField.setActiveFilter(activeState);
    m_contentAssistField.doSearch(false, false);
  }

  public final boolean isAllowCustomText() {
    return m_allowCustomText;
  }

  public IContentAssistField<?, LOOKUP_KEY> getContentAssistField() {
    return m_contentAssistField;
  }

  protected void updateStatus(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    if (result != null && result.getException() instanceof FutureCancelledError) {
      return;
    }

    List<? extends ILookupRow<LOOKUP_KEY>> rows = null;
    Throwable exception = null;
    String searchText = null;
    if (result != null) {
      rows = result.getLookupRows();
      exception = result.getException();
      searchText = result.getSearchParam().getSearchQuery();
    }
    if (rows == null) {
      rows = CollectionUtility.emptyArrayList();
    }
    String statusText = null;
    int severity = IStatus.INFO;
    if (exception != null) {
      if (exception instanceof ProcessingException) {
        statusText = ((ProcessingException) exception).getStatus().getMessage();
      }
      else {
        statusText = exception.getMessage();
      }
      severity = IStatus.ERROR;
    }
    else if (rows.isEmpty()) {
      if (getContentAssistField().getWildcard().equals(searchText)) {
        statusText = TEXTS.get("SmartFieldNoDataFound");
      }
      else {
        statusText = TEXTS.get("SmartFieldCannotComplete", (searchText == null) ? ("") : (searchText));
      }
      severity = IStatus.WARNING;
    }
    else if (rows.size() > m_contentAssistField.getBrowseMaxRowCount()) {
      statusText = TEXTS.get("SmartFieldMoreThanXRows", "" + m_contentAssistField.getBrowseMaxRowCount());
      severity = IStatus.INFO;
    }
    if (statusText != null) {
      setStatus(new Status(statusText, severity));
    }
    else {
      setStatus(null);
    }
  }

}
