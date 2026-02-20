/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import java.lang.constant.Constable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * {@link Map} backed by another {@link Map} providing default values. The default value {@link Map} will remain unchanged but allows to use a static {@link Map} instance for multiple instances of {@code DefaultValueMap} to reduce memory
 * consumption.
 */
@SuppressWarnings("squid:S2160") // no additional equals impl is necessary for this class as AbstractMap provides one which relies on entrySet (implemented by this class respecting the variables of this class)
public class DefaultValueMap<K, V> extends AbstractMap<K, V> {

  /**
   * Static object put into {@link #m_values} to mark an object contained in the {@link #m_defaultValues} as removed.
   */
  protected static final Object REMOVED_MARKER = new Object();

  /**
   * Default values contained in many instances of this class.
   */
  protected final Map<K, V> m_defaultValues;

  /**
   * This is the main map containing all non-default-values, also removed default values are contained marked with the {@link #REMOVED_MARKER}.
   * Entries in this map overrule entries in {@link #m_defaultValues}.
   */
  protected final Map<K, Object> m_values;

  /**
   *
   * @param defaultValues
   *     pre-filled entries for this {@link Map}
   */
  public DefaultValueMap(Map<K, V> defaultValues) {
    this(defaultValues, false);
  }

  /**
   *
   * @param defaultValues
   *     potential default values for this {@link Map}
   * @param startEmpty
   *     whether {@link Map} is initially empty (true) or should be pre-filled with the default values
   */
  public DefaultValueMap(Map<K, V> defaultValues, boolean startEmpty) {
    m_defaultValues = defaultValues;
    m_values = new HashMap<>();

    if (startEmpty) {
      // mark all default values as removed in the initial values
      // assume the default values are filled shortly after creation otherwise this instance is going to consume more memory than a regular HashMap
      m_defaultValues.keySet().forEach(k -> m_values.put(k, REMOVED_MARKER));
    }
  }

  @Override
  public int size() {
    return m_defaultValues.keySet().stream()
        .mapToInt(k -> m_values.containsKey(k) ? 0 : 1)
        .sum() // all default values as long they are not contained in the additional values
        + m_values.values().stream()
        .mapToInt(o -> o == REMOVED_MARKER ? 0 : 1)
        .sum(); // all additional values as long as they are not removed
  }

  @Override
  public boolean isEmpty() {
    return (m_defaultValues.isEmpty() && m_values.isEmpty()) // either default and additional values are empty
        || size() == 0; // or size is 0
  }

  @Override
  public boolean containsKey(Object key) {
    boolean valuesContainsKey = m_values.containsKey(key);
    //noinspection SuspiciousMethodCalls
    return (m_defaultValues.containsKey(key) && !valuesContainsKey) || (valuesContainsKey && m_values.get(key) != REMOVED_MARKER);
  }

  @Override
  public boolean containsValue(Object value) {
    return m_defaultValues.entrySet().stream().anyMatch(e -> Objects.equals(e.getValue(), value) && !m_values.containsKey(e.getKey())) || m_values.containsValue(value);
  }

  @Override
  public V get(Object key) {
    //noinspection SuspiciousMethodCalls
    if (m_values.containsKey(key)) {
      Object value = m_values.get(key);
      //noinspection unchecked
      return value == REMOVED_MARKER ? null : (V) value;
    }
    // either contained here, might return null if not contained
    return m_defaultValues.get(key);
  }

  @Override
  public V put(K key, V value) {
    boolean defaultValuesContainsKey = m_defaultValues.containsKey(key);
    if (defaultValuesContainsKey && isDefaultValue(key, value) /* must be same, equals would not be sufficient */) {
      //noinspection unchecked
      return m_values.containsKey(key)
          ? (V) Optional.ofNullable(m_values.remove(key)).filter(p -> p != REMOVED_MARKER).orElse(null) // return the previous non-default value, null for internal REMOVED_MARKER (and also if value was previously null)
          : m_defaultValues.get(key); // value was previously the default value which it is also now (same value), just return previous value
    }
    Object previousValue = Optional.ofNullable(m_values.put(key, value)) // if m_values previously contained the key: use existing previous value
        .orElse(defaultValuesContainsKey ? m_defaultValues.get(key) : null); // else: load previous value from default values (if any)
    //noinspection unchecked
    return previousValue == REMOVED_MARKER ? null : (V) previousValue; // never return the REMOVED_MARKER to the outside world
  }

  protected boolean isDefaultValue(K key, V value) {
    Object defaultValue = m_defaultValues.get(key);
    if (defaultValue == value) {
      return true;
    }
    else if (defaultValue == null || value == null) {
      return false; // null-safety for following code
    }
    if (defaultValue instanceof Constable) {
      return Objects.equals(defaultValue, value); // equals is alright for constant types
    }
    return false;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new P_EntrySet();
  }

  protected class P_EntrySet extends AbstractSet<Entry<K, V>> {

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new P_EntrySetIterator();
    }

    @Override
    public int size() {
      return DefaultValueMap.this.size();
    }

    @Override
    public boolean isEmpty() {
      return DefaultValueMap.this.isEmpty();
    }
  }

  protected class P_EntrySetIterator implements Iterator<Entry<K, V>> {
    private Entry<K, V> current = null;
    private K lastKey = null;
    private boolean advanced = false;

    private Iterator<Entry<K, V>> defaultValuesEntries = m_defaultValues.entrySet().iterator();
    private Iterator<Entry<K, Object>> valuesEntries;

    @Override
    public boolean hasNext() {
      if (!advanced) {
        advanceInternal();
      }
      return current != null;
    }

    private void advanceInternal() {
      Object previous = current;
      // advance through default values
      while (defaultValuesEntries != null && defaultValuesEntries.hasNext() && (previous == current || m_values.containsKey(current.getKey()))) { // skip the default values contained as additional values
        current = defaultValuesEntries.next();
      }
      if ((previous == current || m_values.containsKey(current.getKey())) && defaultValuesEntries != null) {
        defaultValuesEntries = null; // default values entries are all consumed, not necessary anymore
        valuesEntries = m_values.entrySet().iterator(); // now consume the additional values entries
        previous = current;
      }
      // advance through additional values
      while (valuesEntries != null && valuesEntries.hasNext() && (previous == current || m_values.get(current.getKey()) == REMOVED_MARKER)) { // skip the additional values marked as removed
        //noinspection unchecked
        current = (Entry<K, V>) valuesEntries.next();
      }
      if (previous == current || m_values.get(current.getKey()) == REMOVED_MARKER) {
        current = null;
      }
      advanced = true;
    }

    @Override
    public Entry<K, V> next() {
      if (!advanced) {
        advanceInternal();
      }
      advanced = false;
      if (current == null) {
        throw new NoSuchElementException();
      }
      lastKey = current.getKey();
      // return the previous entry
      return current;
    }

    @Override
    public void remove() {
      if (lastKey == null) {
        throw new IllegalStateException();
      }
      if (m_defaultValues.containsKey(lastKey)) {
        // potentially overwrite previously set value (previous value might not have been the default value)
        m_values.put(lastKey, REMOVED_MARKER);
      }
      else {
        valuesEntries.remove();
      }
      lastKey = null;
    }
  }
}
