/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.context;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Property observer fires property "values" of data type Map<String,Object>
 */
public class SharedVariableMap implements Serializable, Map<String, Object> {
  private static final long serialVersionUID = 1L;
  public static final String PROP_VALUES = "values";

  private final Map<String, Object> m_variables;
  private transient BasicPropertySupport m_propertySupport;

  public SharedVariableMap() {
    m_variables = new HashMap<>();
    m_propertySupport = new BasicPropertySupport(this);
  }

  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    m_propertySupport = new BasicPropertySupport(this);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * Update values of this variable map with the new one.
   */
  public void updateInternal(Map<String, Object> newMap) {
    if (m_variables.equals(newMap)) {
      return; // nothing changed
    }

    m_variables.clear();
    putAll(newMap); // fires a value changed event
  }

  private void fireValuesChanged() {
    m_propertySupport.firePropertyChange(PROP_VALUES, null, CollectionUtility.copyMap(m_variables));
  }

  /**
   * Fires a change event
   */
  @Override
  public void clear() {
    m_variables.clear();
    fireValuesChanged();
  }

  @Override
  public boolean containsKey(Object key) {
    return m_variables.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return m_variables.containsValue(value);
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return CollectionUtility.hashSet(m_variables.entrySet());
  }

  @Override
  public Object get(Object key) {
    return m_variables.get(key);
  }

  @Override
  public boolean isEmpty() {
    return m_variables.isEmpty();
  }

  @Override
  public Set<String> keySet() {
    return CollectionUtility.hashSet(m_variables.keySet());
  }

  /**
   * Fires a change event
   */
  @Override
  public Object put(String key, Object value) {
    Object o = m_variables.put(key, value);
    fireValuesChanged();
    return o;
  }

  /**
   * Fires a change event
   */
  @Override
  public void putAll(Map<? extends String, ?> m) {
    m_variables.putAll(m);
    fireValuesChanged();
  }

  /**
   * Fires a change event
   */
  @Override
  public Object remove(Object key) {
    Object o = m_variables.remove(key);
    fireValuesChanged();
    return o;
  }

  @Override
  public int size() {
    return m_variables.size();
  }

  @Override
  public Collection<Object> values() {
    return CollectionUtility.arrayList(m_variables.values());
  }

  @Override
  public String toString() {
    return m_variables.toString();
  }
}
