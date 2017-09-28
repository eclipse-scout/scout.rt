/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
