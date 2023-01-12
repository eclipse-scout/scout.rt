/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.holders;

import java.util.Collections;
import java.util.EnumSet;

import org.eclipse.scout.rt.platform.holders.IBeanArrayHolder.State;

public class BeanArrayHolderFilter<T> {
  private final IBeanArrayHolder<T> m_beanArray;
  private final EnumSet<State> m_states = EnumSet.noneOf(State.class);

  public BeanArrayHolderFilter(IBeanArrayHolder<T> beanArray, State... states) {
    m_beanArray = beanArray;
    if (states != null) {
      Collections.addAll(m_states, states);
    }
  }

  public IBeanArrayHolder<T> getBeanArrayHolder() {
    return m_beanArray;
  }

  public T[] getFilteredBeans() {
    return m_beanArray.getBeans((m_states.toArray(new State[0])));
  }
}
