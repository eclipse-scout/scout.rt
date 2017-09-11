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

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class SmartFieldResult<V> {

  private final ISmartFieldDataFetchResult<V> m_result;

  public SmartFieldResult(ISmartFieldDataFetchResult<V> result) {
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

  public boolean isByRec() {
    return m_result.getSearchParam().isByParentSearch();
  }

  public V getRec() {
    return m_result.getSearchParam().getParentKey();
  }

}
