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
package org.eclipse.scout.rt.platform.holders;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;

/**
 * @param <T>
 *          the type of beans stored in the holder
 * @since 3.0
 */
public class BeanArrayHolder<T> implements IBeanArrayHolder<T>, Serializable {
  private static final long serialVersionUID = 1L;
  private final List<T> m_list;
  private final Map<T, State> m_stateList;
  private final Class<T> m_clazz;

  public BeanArrayHolder(Class<T> clazz) {
    m_clazz = clazz;
    m_list = new ArrayList<>();
    m_stateList = new HashMap<>();
  }

  @Override
  public T addBean() {
    T ret = null;
    try {
      ret = m_clazz.newInstance();
    }
    catch (ReflectiveOperationException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    m_list.add(ret);
    m_stateList.put(ret, State.NON_CHANGED);
    return ret;
  }

  @Override
  public void ensureSize(int size) {
    while (m_list.size() < size) {
      addBean();
    }
    while (m_list.size() > size) {
      removeBean(m_list.size() - 1);
    }
  }

  public void removeBean(int index) {
    T ret = m_list.remove(index);
    m_stateList.remove(ret);
  }

  @Override
  public Class<T> getHolderType() {
    return m_clazz;
  }

  @Override
  public int getBeanCount(State... states) {
    if (states == null || states.length == 0) {
      return m_list.size();
    }
    EnumSet<State> state = EnumSet.noneOf(State.class);
    Collections.addAll(state, states);
    if (state.isEmpty() || (state.contains(State.INSERTED) && state.contains(State.UPDATED)
        && state.contains(State.DELETED) && state.contains(State.NON_CHANGED))) {
      return m_list.size();
    }
    int ret = 0;
    for (T b : m_list) {
      if (state.contains(m_stateList.get(b))) {
        ret++;
      }
    }
    return ret;
  }

  @Override
  public T[] getBeans(State... states) {
    List<T> ret = new ArrayList<>();
    EnumSet<State> state = EnumSet.noneOf(State.class);
    if (states != null) {
      Collections.addAll(state, states);
    }
    if (state.isEmpty() || (state.contains(State.INSERTED) && state.contains(State.UPDATED)
        && state.contains(State.DELETED) && state.contains(State.NON_CHANGED))) {
      ret = m_list;
    }
    else {
      for (T b : m_list) {
        if (state.contains(m_stateList.get(b))) {
          ret.add(b);
        }
      }
    }
    @SuppressWarnings("unchecked")
    T[] result = ret.toArray((T[]) Array.newInstance(m_clazz, ret.size()));
    return result;
  }

  @Override
  public State getRowState(T bean) {
    return m_stateList.get(bean);
  }

  @Override
  public void setRowState(T bean, State state) {
    m_stateList.put(bean, state);
  }
}
