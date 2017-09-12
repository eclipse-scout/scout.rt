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

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;

public abstract class AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> implements ISmartFieldLookupRowFetcher<LOOKUP_KEY> {

  private final BasicPropertySupport m_propertySupport;
  private final ISmartField<LOOKUP_KEY> m_smartField;

  public AbstractSmartFieldLookupRowFetcher(ISmartField<LOOKUP_KEY> smartField) {
    m_smartField = smartField;
    m_propertySupport = new BasicPropertySupport(this);
  }

  /**
   * @param listener
   * @see BasicPropertySupport#addPropertyChangeListener(PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see BasicPropertySupport#removePropertyChangeListener(PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see BasicPropertySupport#addPropertyChangeListener(String, PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see BasicPropertySupport#removePropertyChangeListener(String, PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  public ISmartField<LOOKUP_KEY> getSmartField() {
    return m_smartField;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ISmartFieldDataFetchResult<LOOKUP_KEY> getResult() {
    return (ISmartFieldDataFetchResult<LOOKUP_KEY>) m_propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  @Override
  public ISmartFieldDataFetchResult<LOOKUP_KEY> newResult(String searchText, boolean selectCurrentValue) {
    String wildcard = getSmartField().getWildcard();
    ISmartFieldSearchParam<LOOKUP_KEY> param = SmartFieldSearchParam.createTextParam(wildcard, searchText, selectCurrentValue);
    return new SmartFieldDataFetchResult<>(null, null, param);
  }

  protected void setResult(ISmartFieldDataFetchResult<LOOKUP_KEY> result) {
    // Always propagate the event of an executed search to the listeners even if the search result did not change. Thus, the proposal popup is opened for every search.
    m_propertySupport.setPropertyAlwaysFire(PROP_SEARCH_RESULT, result);
  }

  @Override
  public String getLastSearchText() {
    if (getResult() != null) {
      return getResult().getSearchParam().getSearchQuery();
    }
    return null;
  }
}
