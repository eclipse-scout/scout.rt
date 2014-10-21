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

import org.eclipse.scout.commons.beans.BasicPropertySupport;

/**
 *
 */
public abstract class AbstractContentAssistFieldLookupRowFetcher<KEY_TYPE> implements IContentAssistFieldLookupRowFetcher<KEY_TYPE> {

  private BasicPropertySupport propertySupport;
  private final IContentAssistField<?, KEY_TYPE> m_contentAssistField;

  public AbstractContentAssistFieldLookupRowFetcher(IContentAssistField<?, KEY_TYPE> contentAssistField) {
    m_contentAssistField = contentAssistField;
    propertySupport = new BasicPropertySupport(this);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  public IContentAssistField<?, KEY_TYPE> getContentAssistField() {
    return m_contentAssistField;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldDataFetchResult<KEY_TYPE> getResult() {
    return (IContentAssistFieldDataFetchResult<KEY_TYPE>) propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  protected void setResult(IContentAssistFieldDataFetchResult<KEY_TYPE> result) {
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
