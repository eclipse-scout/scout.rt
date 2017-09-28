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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldDataFetchResult;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartField2Result<V> {

  private final IContentAssistFieldDataFetchResult<V> m_result;

  public SmartField2Result(IContentAssistFieldDataFetchResult<V> result) {
    m_result = result;
  }

  public List<ILookupRow<V>> getLookupRows() {
    return m_result.getLookupRows();
  }

  public Throwable getException() {
    return m_result.getException();
  }

  public String getSearchText() {
    return m_result.getSearchParam().getSearchText();
  }

  public V getParentKey() {
    return m_result.getSearchParam().getParentKey();
  }

  public boolean isByParentKeySearch() {
    return m_result.getSearchParam().isByParentKeySearch();
  }

  public V getKey() {
    return m_result.getSearchParam().getKey();
  }

  public boolean isByKeySearch() {
    return m_result.getSearchParam().isByKeySearch();
  }

}
