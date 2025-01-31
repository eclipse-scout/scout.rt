/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class LookupCallResult<LOOKUP_KEY> implements ILookupCallResult<LOOKUP_KEY> {

  private final List<ILookupRow<LOOKUP_KEY>> m_lookupRows;
  private final IQueryParam<LOOKUP_KEY> m_queryParam;
  private final Throwable m_exception;

  public LookupCallResult(List<ILookupRow<LOOKUP_KEY>> lookupRows, IQueryParam<LOOKUP_KEY> queryParam, Throwable exception) {
    m_lookupRows = lookupRows == null ? Collections.emptyList() : lookupRows;
    m_queryParam = queryParam;
    m_exception = exception;
  }

  @Override
  public IQueryParam<LOOKUP_KEY> getQueryParam() {
    return m_queryParam;
  }

  @Override
  public Throwable getException() {
    return m_exception;
  }

  @Override
  public List<ILookupRow<LOOKUP_KEY>> getLookupRows() {
    return m_lookupRows;
  }
}
