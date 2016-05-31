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

public interface IContentAssistFieldLookupRowFetcher<LOOKUP_KEY> {

  String PROP_SEARCH_RESULT = "searchResult";

  /**
   * @param listener
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * @param listener
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * @param propertyName
   * @param listener
   */
  void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * @param propertyName
   * @param listener
   */
  void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

  void update(IContentAssistSearchParam<LOOKUP_KEY> searchParam, boolean synchronous);

  IContentAssistFieldDataFetchResult<LOOKUP_KEY> getResult();

  IContentAssistFieldDataFetchResult<LOOKUP_KEY> newResult(String searchText, boolean selectCurrentValue);

  String getLastSearchText();

}
