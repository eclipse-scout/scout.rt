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
package org.eclipse.scout.commons.holders;

import java.util.EnumSet;

import org.eclipse.scout.commons.holders.IBeanArrayHolder.State;

public class BeanArrayHolderFilter<T> {
  private final IBeanArrayHolder<T> m_beanArray;
  private final EnumSet<State> m_states = EnumSet.noneOf(State.class);

  public BeanArrayHolderFilter(IBeanArrayHolder<T> beanArray, State... states) {
    m_beanArray = beanArray;
    if (states != null) {
      for (State s : states) {
        m_states.add(s);
      }
    }
  }

  public IBeanArrayHolder<T> getBeanArrayHolder() {
    return m_beanArray;
  }

  public T[] getFilteredBeans() {
    return m_beanArray.getBeans((m_states.toArray(new State[0])));
  }
}
