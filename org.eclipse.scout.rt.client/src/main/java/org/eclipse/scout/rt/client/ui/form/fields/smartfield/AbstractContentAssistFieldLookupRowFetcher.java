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

import java.beans.PropertyChangeListener;
import java.util.Collections;

import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractContentAssistFieldLookupRowFetcher<LOOKUP_KEY> implements IContentAssistFieldLookupRowFetcher<LOOKUP_KEY> {

  private BasicPropertySupport propertySupport;
  private final IContentAssistField<?, LOOKUP_KEY> m_contentAssistField;

  public AbstractContentAssistFieldLookupRowFetcher(IContentAssistField<?, LOOKUP_KEY> contentAssistField) {
    m_contentAssistField = contentAssistField;
    propertySupport = new BasicPropertySupport(this);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.rt.platform.reflect.BasicPropertySupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.rt.platform.reflect.BasicPropertySupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.rt.platform.reflect.BasicPropertySupport#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.rt.platform.reflect.BasicPropertySupport#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  public IContentAssistField<?, LOOKUP_KEY> getContentAssistField() {
    return m_contentAssistField;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldDataFetchResult<LOOKUP_KEY> getResult() {
    return (IContentAssistFieldDataFetchResult<LOOKUP_KEY>) propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  @Override
  public IContentAssistFieldDataFetchResult<LOOKUP_KEY> newResult(String searchText, boolean selectCurrentValue) {
    return new ContentAssistFieldDataFetchResult<LOOKUP_KEY>(Collections.<ILookupRow<LOOKUP_KEY>> emptyList(), null, searchText, selectCurrentValue);
  }

  protected void setResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    // Always propagate the event of an executed search to the listeners even if the search result did not change. Thus, the proposal popup is opened for every search.
    propertySupport.setPropertyAlwaysFire(PROP_SEARCH_RESULT, result);
  }

  @Override
  public String getLastSearchText() {
    if (getResult() != null) {
      return getResult().getSearchText();
    }
    return null;
  }
}
