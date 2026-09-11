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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

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
    boolean valuesContainsKey = m_values.containsKey(key);
    Object previousValue = m_values.put(key, value); // first: use existing previous value from m_values (might have known this key already)
    if (!valuesContainsKey && defaultValuesContainsKey) {
      previousValue = m_defaultValues.get(key); // however: if values did not contain the key previously and it is a default value; return the default value instead
    }
    //noinspection unchecked
    return previousValue == REMOVED_MARKER ? null : (V) previousValue; // never return the REMOVED_MARKER to the outside world
  }

  protected boolean isDefaultValue(K key, V value) {
    Object defaultValue = m_defaultValues.get(key);
    if (defaultValue == value) {
      return true; // they are the same
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
    private Entry<K, ? super V> current = null;
    private boolean advanced = false;

    private Iterator<Entry<K, V>> defaultValuesEntries = m_defaultValues.entrySet().iterator();
    private Iterator<Entry<K, Object>> valuesEntries;

    /**
     * When previously default values are added during iteration to {@link #m_values} by {@link Entry#setValue(Object)} they must not be iterated again (same key).
     */
    private Set<K> skipValueKeys = Collections.emptySet();

    @Override
    public boolean hasNext() {
      if (!advanced) {
        advanceInternal();
      }
      return current != null;
    }

    protected void advanceInternal() {
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
      while (valuesEntries != null && valuesEntries.hasNext() && (previous == current || m_values.get(current.getKey()) == REMOVED_MARKER || skipValueKeys.contains(current.getKey()))) { // skip the values marked as removed and values which are explicitly marked to be skipped (see above)
        current = valuesEntries.next();
      }
      if (previous == current || m_values.get(current.getKey()) == REMOVED_MARKER) {
        current = null;
      }
      advanced = true;
    }

    protected Entry<K, V> wrapEntry(Entry<K, ? super V> entry) {
      return new P_Entry(entry);
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
      // return the previous entry from current
      return wrapEntry(current);
    }

    @Override
    public void remove() {
      if (defaultValuesEntries != null) { // we are iterating the default values
        if (m_values.get(current.getKey()) == REMOVED_MARKER) {
          // is already removed
          throw new IllegalStateException();
        }
        m_values.put(current.getKey(), REMOVED_MARKER);
      }
      else if (m_defaultValues.containsKey(current.getKey())) { // iterating the additional values, but value is contained also in default values
        if (current.getValue() == REMOVED_MARKER) {
          // is already removed
          throw new IllegalStateException();
        }
        // potentially overwrite previously set value (previous value might not have been the default value)
        //noinspection unchecked
        ((Entry<K, Object>) current).setValue(REMOVED_MARKER);
      }
      else { // iterating the additional values and value is not contained in default values, just remove
        valuesEntries.remove();
      }
    }

    protected class P_Entry implements Map.Entry<K, V> {
      private final Entry<K, ? super V> m_entry;
      private V newValue;

      protected P_Entry(Entry<K, ? super V> entry) {
        m_entry = entry;
      }

      @Override
      public K getKey() {
        return m_entry.getKey();
      }

      @Override
      public V getValue() {
        //noinspection unchecked
        return (V) ObjectUtility.nvlOpt(newValue, m_entry::getValue);
      }

      @Override
      public V setValue(V value) {
        Assertions.assertSame(current, m_entry);
        if (defaultValuesEntries != null) { // we are iterating the default values
          V previous = getValue(); // remember the previous value
          put(getKey(), value); // possibly put it to the additional values (even though method includes additional checks, e.g. if it is just the default value itself it is not added there)
          if (skipValueKeys.isEmpty()) {
            skipValueKeys = new HashSet<>(); // lazy init the skipValueKeys
          }
          skipValueKeys.add(getKey()); // key should not be seen again during additional values iteration
          newValue = value; // remember the new value (value may be changed several times)
          return previous;
        }
        else {
          //noinspection unchecked
          return (V) m_entry.setValue(value); // best-effort: just pass through; actually if the new value is the default value we will store it as additional value (the only possibility were default value is duplicated)
        }
      }

      @Override
      public boolean equals(Object obj0) {
        if (obj0 == this) {
          return true;
        }
        else if (obj0 instanceof Map.Entry otherEntry) {
          return Objects.equals(getKey(), otherEntry.getKey()) && Objects.equals(getValue(), otherEntry.getValue());
        }
        return false;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
      }
    }
  }
}
