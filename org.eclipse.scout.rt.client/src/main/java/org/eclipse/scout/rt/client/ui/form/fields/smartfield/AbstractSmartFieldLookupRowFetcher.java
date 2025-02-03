/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;

public abstract class AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> implements ISmartFieldLookupRowFetcher<LOOKUP_KEY> {

  private final BasicPropertySupport m_propertySupport;
  private final ISmartField<LOOKUP_KEY> m_smartField;

  public AbstractSmartFieldLookupRowFetcher(ISmartField<LOOKUP_KEY> smartField) {
    m_smartField = smartField;
    m_propertySupport = new BasicPropertySupport(this);
  }

  /**
   * @see BasicPropertySupport#addPropertyChangeListener(PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @see BasicPropertySupport#removePropertyChangeListener(PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public ISmartField<LOOKUP_KEY> getSmartField() {
    return m_smartField;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ILookupCallResult<LOOKUP_KEY> getResult() {
    return (ILookupCallResult<LOOKUP_KEY>) m_propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  protected void setResult(ILookupCallResult<LOOKUP_KEY> result) {
    // Always propagate the event of an executed search to the listeners even if the search result did not change. Thus, the proposal popup is opened for every search.
    m_propertySupport.setPropertyAlwaysFire(PROP_SEARCH_RESULT, result);
  }
}
