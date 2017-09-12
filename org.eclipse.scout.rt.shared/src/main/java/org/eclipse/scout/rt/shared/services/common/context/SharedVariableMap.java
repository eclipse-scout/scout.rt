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

  private int m_version;
  private Map<String, Object> m_variables;
  private transient BasicPropertySupport m_propertySupport;

  public SharedVariableMap() {
    m_version = 0;
    m_variables = new HashMap<>();
    m_propertySupport = new BasicPropertySupport(this);
  }

  public SharedVariableMap(SharedVariableMap map) {
    m_version = map.m_version;
    m_variables = CollectionUtility.copyMap(map.m_variables);
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
   * Update values of this variable map with the new one if version of new map is newer <br>
   * Does not fire a change event
   */
  public void updateInternal(SharedVariableMap newMap) {
    if (newMap.getVersion() != getVersion()) {
      m_variables = CollectionUtility.copyMap(newMap.m_variables);
      m_version = newMap.getVersion();
    }
  }

  /**
   * @return the version seq of the map state. This version number is changed every time the variable map changes. Note
   *         that even a different number means that the version changed, it must not be higher, just different.
   *         <p>
   *         see https://bugs.eclipse.org/bugs/show_bug.cgi?id=358344
   */
  public int getVersion() {
    return m_version;
  }

  private void mapChanged() {
    m_version++;
    if (m_propertySupport != null) {
      m_propertySupport.firePropertyChange("values", null, CollectionUtility.copyMap(m_variables));
    }
  }

  /*
   * Map implementation
   */
  /**
   * Fires a change event
   */
  @Override
  public void clear() {
    m_variables.clear();
    mapChanged();
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
    mapChanged();
    return o;
  }

  /**
   * Fires a change event
   */
  @Override
  public void putAll(Map<? extends String, ?> m) {
    m_variables.putAll(m);
    mapChanged();
  }

  /**
   * Fires a change event
   */
  @Override
  public Object remove(Object key) {
    Object o = m_variables.remove(key);
    mapChanged();
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
