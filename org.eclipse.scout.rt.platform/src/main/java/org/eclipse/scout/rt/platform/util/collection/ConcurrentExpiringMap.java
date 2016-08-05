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
package org.eclipse.scout.rt.platform.util.collection;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe concurrent map with a time to live behavior and an optional size bound. This class is just a decorator
 * for any concurrent map. The default constructor returns an instance that updates the timeout for an entry only on a
 * write access. There is a special additional method {@link #getAndTouch(Object)} that updates the timeout also an read
 * access.
 * <p>
 * If the <tt>targetSize</tt> property is set, the maximum number of cached values is bounded. The provided size bound
 * is <em>not</em> enforced and is just a guidance value. In fact, the map grows up to <tt>overflowSize</tt> till it is
 * shrunk back to the targeted size.
 * <p>
 * If the <tt>touchOnGet</tt> property is set, the {@link #get(Object)} operation does the same as
 * {@link #getAndTouch(Object)}.
 * <p>
 * If the <tt>touchOnIterate</tt> property is set, iterating through the map does update the timeout. This has no effect
 * to {@link #containsKey(Object)}, {@link #containsValue(Object)}, {@link #size()} or {@link #isEmpty()}.
 * <p>
 * Every time an entry in the map is evicted, {@link #execEntryEvicted(Object, Object)} is called.
 * <p>
 * <em>Important</em>: The method {@link #size()} does not check if entries are expired. Else it would have to iterate
 * though the whole map at each call. Therefore the following may be true: {@code size()==1 && isEmpty()}. Like the
 * implementation in {@link AbstractCollection#toArray()} one should be prepared for such a behavior.
 * <p>
 * This class does <em>not</em> allow <tt>null</tt> as a key, but is <em>allows</em> null values. Inserting a null key
 * result in a {@link NullPointerException} as specified in {@link Map}.
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em> methods of the {@link Map} and
 * {@link Iterator} interfaces.
 *
 * @param <K>
 *          the type of keys maintained by this map
 * @param <V>
 *          the type of mapped values
 * @since 5.2
 */
public class ConcurrentExpiringMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
  private final ConcurrentMap<K, ExpiringElement<V>> m_elementMap;
  private final long m_timeToLive;
  private final boolean m_touchOnGet;
  private final boolean m_touchOnIterate;

  private final int m_targetSize;
  private final int m_overflowSize;
  private final Lock m_validateSizeLock = new ReentrantLock();

  /**
   * Creates a new map with a default timeout of 60 seconds and no target size.
   */
  public ConcurrentExpiringMap() {
    this(1, TimeUnit.MINUTES);
  }

  /**
   * <b>TTL cache</b>
   * <p>
   * Creates a new map in which entries expire after a given time to live.
   *
   * @param timeToLiveDuration
   *          if greater than zero, entries expire after the given duration
   * @param timeToLiveUnit
   *          unit of timeToLive
   */
  public ConcurrentExpiringMap(long timeToLiveDuration, TimeUnit timeToLiveUnit) {
    this(new ConcurrentHashMap<K, ExpiringElement<V>>(), timeToLiveUnit.toMillis(timeToLiveDuration), false, false, 0, 0);
  }

  /**
   * <b>LRU cache</b>
   * <p>
   * Creates a new map in which entries expire after a given time to live. If an entry is accessed the timestamp of this
   * entry is reset. If the map get bigger than 1.5 times the given targetSize, old entries are evicted until the map is
   * at most the given targetSize.
   *
   * @param timeToLiveDuration
   *          if greater than zero, entries expire after the given duration
   * @param timeToLiveUnit
   *          unit of timeToLive
   * @param targetSize
   *          if greater than zero, entries may be evicted at a put operation until the map reaches this size
   */
  public ConcurrentExpiringMap(long timeToLiveDuration, TimeUnit timeToLiveUnit, int targetSize) {
    this(new ConcurrentHashMap<K, ExpiringElement<V>>(), timeToLiveUnit.toMillis(timeToLiveDuration), true, false, targetSize, defaultOverflowSize(targetSize));
  }

  /**
   * <b>Copy constructor</b>
   * <p>
   * This constructor is useful if the timeToLiveDuration should be changed.
   *
   * @param map
   *          instance to copy
   * @param timeToLiveDuration
   *          if greater than zero, entries expire after the given duration
   * @param timeToLiveUnit
   *          unit of timeToLive
   */
  public ConcurrentExpiringMap(ConcurrentExpiringMap<K, V> map, long timeToLiveDuration, TimeUnit timeToLiveUnit) {
    this(map.m_elementMap, timeToLiveUnit.toMillis(timeToLiveDuration), map.m_touchOnGet, map.m_touchOnIterate, map.m_targetSize, map.m_overflowSize);
  }

  /**
   * <b>Copy constructor</b>
   * <p>
   * This constructor is useful if the targetSize should be changed. The ration between targetSize and overflowSize is
   * preserved.
   *
   * @param map
   *          instance to copy
   * @param targetSize
   *          if greater than zero, entries may be evicted at a put operation until the map reaches this size
   */
  public ConcurrentExpiringMap(ConcurrentExpiringMap<K, V> map, int targetSize) {
    this(map.m_elementMap, map.m_timeToLive, map.m_touchOnGet, map.m_touchOnIterate, targetSize, targetSize * map.m_overflowSize / map.m_targetSize);
  }

  /**
   * @param elementMap
   *          {@link ConcurrentMap} that contains {@link ExpiringElement}s
   * @param timeToLiveDurationMillis
   *          if greater than zero, entries expire after the given duration
   * @param touchOnGet
   *          if true, {@link #get(Object)} operation updates the timestamp of an entry
   * @param targetSize
   *          if greater than zero, entries may be evicted at a put operation until the map reaches this size
   */
  public ConcurrentExpiringMap(ConcurrentMap<K, ExpiringElement<V>> elementMap, long timeToLiveDurationMillis, boolean touchOnGet, int targetSize) {
    this(elementMap, timeToLiveDurationMillis, touchOnGet, false, targetSize, defaultOverflowSize(targetSize));
  }

  /**
   * @param elementMap
   *          {@link ConcurrentMap} that contains {@link ExpiringElement}s
   * @param timeToLiveDurationMillis
   *          if greater than zero, entries expire after the given duration
   * @param touchOnGet
   *          if true, {@link #get(Object)} operation updates the timestamp of an entry
   * @param touchOnIterate
   *          if true, iterating through the entries updates the timestamp of entries
   * @param targetSize
   *          if greater than zero, entries may be evicted at a put operation until the map reaches this size
   * @param overflowSize
   *          if greater than zero and the map is bigger than this size, oldest entries are evicted until targetSize is
   *          reached
   * @throws IllegalArgumentException
   *           if targetSize is greater than zero but overflow size is not greater than targetSize
   */
  public ConcurrentExpiringMap(ConcurrentMap<K, ExpiringElement<V>> elementMap, long timeToLiveDurationMillis, boolean touchOnGet, boolean touchOnIterate, int targetSize, int overflowSize) {
    m_elementMap = elementMap;
    m_timeToLive = timeToLiveDurationMillis;
    m_touchOnGet = touchOnGet;
    m_touchOnIterate = touchOnIterate;
    if (overflowSize > 0) {
      if (targetSize <= 0 || targetSize >= overflowSize) {
        throw new IllegalArgumentException("overflowSize is set but targetSize has no valid value");
      }
    }
    m_targetSize = targetSize;
    m_overflowSize = overflowSize;
  }

  private static final int defaultOverflowSize(int targetSize) {
    if (targetSize == 1) {
      // special case for targetSize == 1; return then 2 in order to be strictly greater
      return 2;
    }
    return targetSize * 3 / 2;
  }

  /**
   * @return the decorated map instance
   */
  public ConcurrentMap<K, ExpiringElement<V>> getElementMap() {
    return m_elementMap;
  }

  public long getTimeToLive() {
    return m_timeToLive;
  }

  public boolean isTouchOnGet() {
    return m_touchOnGet;
  }

  public boolean isTouchOnIterate() {
    return m_touchOnIterate;
  }

  public int getTargetSize() {
    return m_targetSize;
  }

  public int getOverflowSize() {
    return m_overflowSize;
  }

  /**
   * <b>Note:</b> The implementation of this method does not check if entries are expired, else it would have to iterate
   * though the whole map at each call. Therefore the following may be true: {@code size()==1 && isEmpty()}. Like the
   * implementation in {@link AbstractCollection#toArray()} one should be prepared for such a behavior.
   */
  @Override
  public int size() {
    return m_elementMap.size();
  }

  @Override
  public boolean isEmpty() {
    return !newEntryIterator(false).hasNext();
  }

  @Override
  public boolean containsValue(Object value) {
    Iterator<Entry<K, V>> i = newEntryIterator(false);
    // code below copy-pasted from super implementation
    if (value == null) {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (e.getValue() == null) {
          return true;
        }
      }
    }
    else {
      while (i.hasNext()) {
        Entry<K, V> e = i.next();
        if (value.equals(e.getValue())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return getElement(key) != null;
  }

  @Override
  public V get(Object key) {
    ExpiringElement<V> e = getElement(key, m_touchOnGet);
    return e != null ? e.getValue() : null;
  }

  /**
   * Like the {@link #get(Object)} operation but forces an update of the timeout
   *
   * @param key
   *          the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key
   */
  public V getAndTouch(Object key) {
    ExpiringElement<V> e = getElement(key, true);
    return e != null ? e.getValue() : null;
  }

  @Override
  public void clear() {
    m_elementMap.clear();
  }

  @Override
  public V put(K key, V value) {
    ExpiringElement<V> e = m_elementMap.put(key, createElement(value));
    validateSize();
    return extractValidElementValue(e);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      m_elementMap.put(e.getKey(), createElement(e.getValue()));
    }
    validateSize();
  }

  @Override
  public V remove(Object key) {
    ExpiringElement<V> e = m_elementMap.remove(key);
    return extractValidElementValue(e);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    ExpiringElement<V> e = m_elementMap.putIfAbsent(key, createElement(value));
    if (e != null && !isElementValid(e)) {
      // timeout
      if (m_elementMap.remove(key, e)) {
        execEntryEvicted(key, e.getValue());
      }
      // retry
      e = m_elementMap.putIfAbsent(key, createElement(value));
    }
    validateSize();
    return e != null ? e.getValue() : null;
  }

  @Override
  public boolean remove(Object key, Object value) {
    ExpiringElement<V> e = getElement(key);
    if (e != null) {
      V currValue = e.getValue();
      if (currValue == value /* null case too */ || (currValue != null && currValue.equals(value))) {
        return m_elementMap.remove(key, e);
      }
    }
    // did not contain mapping / not correct mapping / no remove (see containsKey)
    return false;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    ExpiringElement<V> currElement = getElement(key);
    if (currElement != null) {
      V currValue = currElement.getValue();
      if (currValue == oldValue /* null case too */ || (currValue != null && currValue.equals(oldValue))) {
        return m_elementMap.replace(key, currElement, createElement(newValue));
      }
    }
    // did not contain mapping / not correct mapping / no replace (see containsKey)
    return false;
  }

  @Override
  public V replace(K key, V value) {
    ExpiringElement<V> e = getElement(key);
    if (e != null) {
      e = m_elementMap.replace(key, createElement(value));
    }
    return e != null ? e.getValue() : null;
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  protected ExpiringElement<V> createElement(V value) {
    return new ExpiringElement<V>(value);
  }

  protected ExpiringElement<V> getElement(Object key) {
    return getElement(key, false);
  }

  @SuppressWarnings("unchecked")
  protected ExpiringElement<V> getElement(Object key, boolean touchOnReadAccess) {
    ExpiringElement<V> e = m_elementMap.get(key);
    if (e != null) {
      if (isElementValid(e)) {
        if (touchOnReadAccess) {
          e = touch((K) key, e);
        }
        return e;
      }
      else {
        // timeout
        if (m_elementMap.remove(key, e)) {
          execEntryEvicted((K) key, e.getValue());
        }
      }
    }
    return null;
  }

  protected ExpiringElement<V> touch(K key, ExpiringElement<V> e) {
    while (e != null) {
      if (m_elementMap.replace(key, e, createElement(e.getValue()))) {
        return e;
      }
      // else we retry
      e = m_elementMap.get(key);
    }
    return e;
  }

  protected boolean isElementValid(ExpiringElement<V> element) {
    return m_timeToLive <= 0 || element.getTimestamp() + m_timeToLive > System.currentTimeMillis();
  }

  protected V extractValidElementValue(ExpiringElement<V> element) {
    if (element != null && isElementValid(element)) {
      return element.getValue();
    }
    return null;
  }

  protected void validateSize() {
    // note: in JRE 1.8 the performance of ConcurrentHashMap#size() is increased
    if (m_overflowSize > 0 && m_elementMap.size() >= m_overflowSize) {
      // maximum one thread at the time should shrink the map
      if (m_validateSizeLock.tryLock()) {
        try {
          // recheck size
          if (m_elementMap.size() >= m_overflowSize) {
            evictOldestEntries();
          }
        }
        finally {
          m_validateSizeLock.unlock();
        }
      }
    }
  }

  protected void evictOldestEntries() {
    TreeSet<Entry<K, ExpiringElement<V>>> set = new TreeSet<Entry<K, ExpiringElement<V>>>(new StableTimestampComparator());

    int counter = 0;
    for (Entry<K, ExpiringElement<V>> entry : m_elementMap.entrySet()) {
      entry.getValue().m_iterationIndex = counter;
      set.add(entry);
      counter++;
    }
    int numberOfEntriesToEvict = set.size() - m_targetSize;
    while (numberOfEntriesToEvict > 0) {
      Entry<K, ExpiringElement<V>> oldestEntry = set.pollFirst();
      // try to remove entry from element map
      K key = oldestEntry.getKey();
      ExpiringElement<V> element = oldestEntry.getValue();
      if (m_elementMap.remove(key, element)) {
        numberOfEntriesToEvict--;
        execEntryEvicted(key, element.getValue());
      }
    }
  }

  private class StableTimestampComparator implements Comparator<Entry<K, ExpiringElement<V>>> {

    @Override
    public int compare(Entry<K, ExpiringElement<V>> o1, Entry<K, ExpiringElement<V>> o2) {
      ExpiringElement<V> e1 = o1.getValue();
      ExpiringElement<V> e2 = o2.getValue();
      if (e1.m_timestamp < e2.m_timestamp) {
        return -1;
      }
      else if (e1.m_timestamp > e2.m_timestamp) {
        return 1;
      }
      else {
        return Integer.compare(e1.m_iterationIndex, e2.m_iterationIndex);
      }
    }
  }

  /**
   * Called when the map evicted an entry because its time to live elapsed or because the map is reducing its size.
   *
   * @param key
   * @param value
   */
  protected void execEntryEvicted(K key, V value) {
    // hook method for subclasses
  }

  private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return newEntryIterator(m_touchOnIterate);
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      V currentValue = ConcurrentExpiringMap.this.get(e.getKey());
      Object value = e.getValue();
      return currentValue == value || (currentValue != null && currentValue.equals(value));
    }

    @Override
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      return ConcurrentExpiringMap.this.remove(e.getKey()) != null;
    }

    @Override
    public int size() {
      return ConcurrentExpiringMap.this.size();
    }

    @Override
    public void clear() {
      ConcurrentExpiringMap.this.clear();
    }
  }

  protected Iterator<Map.Entry<K, V>> newEntryIterator(boolean touchOnAccess) {
    return new EntryIterator(touchOnAccess);
  }

  private final class EntryIterator implements Iterator<Map.Entry<K, V>> {
    private final boolean m_touchOnAccess;
    private final Iterator<K> m_elementMapIterator;
    private Map.Entry<K, V> m_nextEntry;
    private Map.Entry<K, V> m_lastReturned;

    public EntryIterator(boolean touchOnAccess) {
      m_touchOnAccess = touchOnAccess;
      m_elementMapIterator = m_elementMap.keySet().iterator();
      advance();
    }

    void advance() {
      while (true) {
        if (m_elementMapIterator.hasNext()) {
          K key = m_elementMapIterator.next();
          // We cannot iterate normally through the entries as we MUST use getElement(key, touch)
          // If we do not, we do not remove entries or update access time in an atomic operation which
          // in turn could remove a new value in case of a bad timing.
          // In contrast to other maps, we can update the map during iterating
          ConcurrentExpiringMap.ExpiringElement<V> element = getElement(key, m_touchOnAccess);
          if (element != null) {
            m_nextEntry = new WriteThroughEntry(key, element.getValue());
            break;
          }
        }
        else {
          m_nextEntry = null;
          break;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return m_nextEntry != null;
    }

    @Override
    public Map.Entry<K, V> next() {
      if (m_nextEntry == null) {
        throw new NoSuchElementException();
      }
      m_lastReturned = m_nextEntry;
      advance();
      return m_lastReturned;
    }

    @Override
    public void remove() {
      if (m_lastReturned == null) {
        throw new IllegalStateException();
      }
      ConcurrentExpiringMap.this.remove(m_lastReturned.getKey());
      m_lastReturned = null;
    }
  }

  private final class WriteThroughEntry extends AbstractMap.SimpleEntry<K, V> {
    private static final long serialVersionUID = 1L;

    WriteThroughEntry(K k, V v) {
      super(k, v);
    }

    @Override
    public V setValue(V value) {
      V v = super.setValue(value);
      ConcurrentExpiringMap.this.put(getKey(), value);
      return v;
    }
  }

  public static class ExpiringElement<V> {
    private final long m_timestamp;
    private final V m_value;

    // temporary value / guarded by m_validateSizeLock
    private int m_iterationIndex;

    public ExpiringElement(V value) {
      m_timestamp = System.currentTimeMillis();
      m_value = value;
    }

    public long getTimestamp() {
      return m_timestamp;
    }

    public V getValue() {
      return m_value;
    }
  }
}
