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

/**
 *
 */

public interface IContentAssistFieldLookupRowFetcher<KEY_TYPE> {

  static final String PROP_SEARCH_RESULT = "searchResult";

  static enum Status {
    Updating,
    Complete
  }

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

  /**
   * @param searchText
   * @param selectCurrentValue
   *          select the current proposal value in the proposal table/tree/custom
   *          If necessary in a tree, load the tree children until the key is found
   * @param synchronous
   *          true to execute the lookup call synchronous
   */
  void update(String searchText, boolean selectCurrentValue, boolean synchronous);

  IContentAssistFieldDataFetchResult<KEY_TYPE> getResult();

  /**
   * @return
   */
  String getLastSearchText();
}
