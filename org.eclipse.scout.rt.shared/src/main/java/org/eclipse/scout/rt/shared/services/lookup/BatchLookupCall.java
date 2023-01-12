/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class BatchLookupCall implements Serializable {
  private static final long serialVersionUID = 0L;

  private final List<ILookupCall<?>> m_calls;

  public BatchLookupCall() {
    m_calls = new ArrayList<>();
  }

  public BatchLookupCall(List<ILookupCall<?>> calls) {
    m_calls = new ArrayList<>(calls);
  }

  public void addLookupCall(ILookupCall<?> call) {
    m_calls.add(call);
  }

  public boolean isEmpty() {
    return m_calls.isEmpty();
  }

  public List<ILookupCall<?>> getCallBatch() {
    return CollectionUtility.arrayList(m_calls);
  }
}
