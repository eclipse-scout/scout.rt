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
 * {@link Map} backed by another {@link Map} with default values (this {@link Map} will remain unchanged but allows using a static {@link Map} for multiple instances to reduce memory consumption).
 */
public class DefaultValueMap extends AbstractMap<String, Object> {

  /**
   * Static object put into {@link #m_additionalValues} to mark an object contained in the {@link #m_defaultValues} as removed.
   */
  protected static final Object REMOVED_MARKER = new Object();

  /**
   * Default values contained in many instances of this class.
   */
  protected final Map<String, Object> m_defaultValues;

  /**
   * Additional or changed values, values in this {@link Map} overwrite values in the {@link #m_defaultValues}.
   */
  protected final Map<String, Object> m_additionalValues;

  /**
   *
   * @param defaultValues
   *     pre-filled entries for this {@link Map}
   */
  public DefaultValueMap(Map<String, Object> defaultValues) {
    this(defaultValues, false);
  }

  /**
   *
   * @param defaultValues
   *     potential default values for this {@link Map}
   * @param startEmpty
   *     whether {@link Map} is initially empty (true) or should be pre-filled with the default values
   */
  public DefaultValueMap(Map<String, Object> defaultValues, boolean startEmpty) {
    m_defaultValues = defaultValues;
    m_additionalValues = new HashMap<>();

    if (startEmpty) {
      // mark all default values as removed in the initial values
      // assume the default values are filled shortly after creation otherwise this instance is going to consume more memory than a regular HashMap
      m_defaultValues.keySet().forEach(k -> m_additionalValues.put(k, REMOVED_MARKER));
    }
  }

  @Override
  public int size() {
    return m_defaultValues.keySet().stream()
        .mapToInt(k -> m_additionalValues.containsKey(k) ? 0 : 1)
        .sum() // all default values as long they are not contained in the additional values
        + m_additionalValues.values().stream()
        .mapToInt(o -> o == REMOVED_MARKER ? 0 : 1)
        .sum(); // all additional values as long as they are not removed
  }

  @Override
  public boolean isEmpty() {
    return (m_defaultValues.isEmpty() && m_additionalValues.isEmpty()) // either default and additional values are empty
        || size() == 0; // or size is 0
  }

  @Override
  public boolean containsKey(Object key) {
    boolean additionalValuesContainsKey = m_additionalValues.containsKey(key);
    //noinspection SuspiciousMethodCalls
    return (m_defaultValues.containsKey(key) && !additionalValuesContainsKey) || (additionalValuesContainsKey && m_additionalValues.get(key) != REMOVED_MARKER);
  }

  @Override
  public boolean containsValue(Object value) {
    return m_defaultValues.entrySet().stream().anyMatch(e -> Objects.equals(e.getValue(), value) && !m_additionalValues.containsKey(e.getKey())) || m_additionalValues.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    //noinspection SuspiciousMethodCalls
    if (m_additionalValues.containsKey(key)) {
      Object value = m_additionalValues.get(key);
      return value == REMOVED_MARKER ? null : value;
    }
    // either contained here, might return null if not contained
    return m_defaultValues.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    boolean additionalValuesContainsKey = m_additionalValues.containsKey(key);
    if (m_defaultValues.containsKey(key)) {
      if (isDefaultValue(key, value)) { // must be same, equals would not be sufficient
        return additionalValuesContainsKey
            ? Optional.ofNullable(m_additionalValues.remove(key)).filter(p -> p != REMOVED_MARKER).orElse(null)
            : m_defaultValues.get(key);
      }
      else if (!additionalValuesContainsKey) {
        m_additionalValues.put(key, value);
        return m_defaultValues.get(key); // load previous value from default values (if any)
      }
    }
    return Optional.ofNullable(m_additionalValues.put(key, value)).filter(p -> p != REMOVED_MARKER).orElse(null);
  }

  protected boolean isDefaultValue(String key, Object value) {
    Object defaultValue = m_defaultValues.get(key);
    if (defaultValue == value) {
      return true;
    }
    else if ((defaultValue == null && value != null) || (defaultValue != null && value == null)) {
      return false; // null-safety for following code
    }
    if (defaultValue instanceof Constable) {
      return Objects.equals(defaultValue, value); // equals is alright for constant types
    }
    return false;
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return new AbstractSet<>() {

      @Override
      public Iterator<Entry<String, Object>> iterator() {
        Iterator<Entry<String, Object>> iterator = new Iterator<>() {
          private Entry<String, Object> current = null;
          private String lastKey = null;
          private boolean advanced = false;

          private Iterator<Entry<String, Object>> defaultValuesEntries = m_defaultValues.entrySet().iterator();
          private Iterator<Entry<String, Object>> additionalValuesEntries;

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
            while (defaultValuesEntries != null && defaultValuesEntries.hasNext() && (previous == current || m_additionalValues.containsKey(current.getKey()))) { // skip the default values contained as additional values
              current = defaultValuesEntries.next();
            }
            if ((previous == current || m_additionalValues.containsKey(current.getKey())) && defaultValuesEntries != null) {
              defaultValuesEntries = null; // default values entries are all consumed, not necessary anymore
              additionalValuesEntries = m_additionalValues.entrySet().iterator(); // now consume the additional values entries
              previous = current;
            }
            // advance through additional values
            while (additionalValuesEntries != null && additionalValuesEntries.hasNext() && (previous == current || m_additionalValues.get(current.getKey()) == REMOVED_MARKER)) { // skip the additional values marked as removed
              current = additionalValuesEntries.next();
            }
            if (previous == current || m_additionalValues.get(current.getKey()) == REMOVED_MARKER) {
              current = null;
            }
            advanced = true;
          }

          @Override
          public Entry<String, Object> next() {
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
              m_additionalValues.put(lastKey, REMOVED_MARKER);
            }
            else {
              additionalValuesEntries.remove();
            }
            lastKey = null;
          }
        };
        return iterator;
      }

      @Override
      public int size() {
        return DefaultValueMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return DefaultValueMap.this.isEmpty();
      }
    };
  }
}
