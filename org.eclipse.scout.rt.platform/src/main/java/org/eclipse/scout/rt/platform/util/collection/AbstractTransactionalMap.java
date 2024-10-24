/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;

/**
 * <p>
 * A thread-safe map with a transactional behavior based on {@link ITransaction}. The map uses a sharedMap to share
 * values between transactions. Access is delegated to a transaction member {@link AbstractMapTransactionMember}. Any
 * changes on the map are local within the current transaction. In commit phase2 of the transaction member, so after
 * committing [transactional] data sources, modified values within the transaction member are committed into the shared
 * map.
 * <p>
 * Use this map for lazy loaded shared caches in a scout server where the cached values origin from a transactional data
 * source.
 * <p>
 * If there were concurrent modifications on the same key in different transactions only the first transaction will be
 * able to commit the change to the shared map. As a default, if a commit fails on a key, the entry is completely
 * removed from the shared map ({@link AbstractMapTransactionMember#changesCommitted}).
 * <p>
 * In order to use this map safely one must conform to the following behavior: <b>If the current transaction changed the
 * value of a key in the transactional source, this maps {@link #remove(Object)} to that key must be called
 * <em>before</em> the value is fetched from that source again.</b> If one fails doing so, there are no guarantees, that
 * values in the shared map reflect values in the transactional source. Note that this behavior is given when using
 * {@link ICache}. See also {@link AbstractMapTransactionMember#changesCommitted}.
 * <p>
 * If the <tt>fastForward</tt> property is set to true, a newly inserted value is directly committed to the shared map
 * if it is consider as a save commit.
 * <p>
 * This class does <em>not</em> allow <tt>null</tt> as a key or value. Inserting a null key or null value result in a
 * {@link NullPointerException} as specified in {@link Map}.
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em> methods of the {@link Map} and
 * {@link Iterator} interfaces.
 * <p>
 * Note that the equality on the values should be well defined, as the implementation uses methods from
 * {@link ConcurrentMap}.
 *
 * @param <K>
 *          the type of keys maintained by this map
 * @param <V>
 *          the type of mapped values
 * @since 5.2
 */
public abstract class AbstractTransactionalMap<K, V> implements Map<K, V> {
  protected final String m_transactionMemberId;
  /**
   * If set to true, before a put operation the shared map is checked if there is already an entry for the given key. If
   * there is no such entry, the new entry is directly put in the shared map.
   */
  protected final boolean m_fastForward;

  protected final AtomicInteger m_insertVersion = new AtomicInteger();
  protected final AtomicInteger m_removeVersion = new AtomicInteger();

  public AbstractTransactionalMap(String transactionMemberId) {
    this(transactionMemberId, true);
  }

  public AbstractTransactionalMap(String transactionMemberId, boolean fastForward) {
    super();
    m_transactionMemberId = transactionMemberId;
    m_fastForward = fastForward;
  }

  public String getTransactionMemberId() {
    return m_transactionMemberId;
  }

  /**
   * This method is call when no scout transaction can be found as a fallback. Implementers are allowed to return a
   * full-featured map, a read only map view or throw an {@link IllegalStateException}. Null is not allowed.
   */
  protected abstract Map<K, V> getSharedMap();

  protected boolean isFastForward() {
    return m_fastForward;
  }

  /**
   * Any inserts in concurrent transactions must be handled as invalid and will not be committed to the shared map (does
   * not apply to inserts from this transaction).
   */
  public void markInsertsDirty() {
    AbstractMapTransactionMember transactionMember = getTransactionMember(true);
    if (transactionMember != null) { // if not transaction, then no transaction member
      transactionMember.markInsertsDirty();
    }
  }

  /**
   * Any removes in concurrent transactions must be handled as invalid and will not be committed to the shared map (does
   * not apply to removes from this transaction).
   */
  public void markRemovesDirty() {
    AbstractMapTransactionMember transactionMember = getTransactionMember(true);
    if (transactionMember != null) { // if not transaction, then no transaction member
      transactionMember.markRemovesDirty();
    }
  }

  protected Map<K, V> getTransactionMap(boolean onlyReadOperation) {
    Map<K, V> m = getTransactionMember(!onlyReadOperation);
    if (m == null) {
      // no transaction, return shared map
      return getSharedMap();
    }
    return m;
  }

  public AbstractMapTransactionMember getTransactionMember(boolean createIfNotExist) {
    ITransaction t = ITransaction.CURRENT.get();
    if (t == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    AbstractMapTransactionMember m = (AbstractMapTransactionMember) t.getMember(getTransactionMemberId());
    if (m == null && createIfNotExist) {
      m = createMapTransactionMember();
      t.registerMember(m);
    }
    return m;
  }

  protected abstract <TM extends Map<K, V> & ITransactionMember> TM createMapTransactionMember();

  @Override
  public int size() {
    return getTransactionMap(true).size();
  }

  @Override
  public boolean isEmpty() {
    return getTransactionMap(true).isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return getTransactionMap(true).containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return getTransactionMap(true).containsValue(value);
  }

  @Override
  public V get(Object key) {
    return getTransactionMap(true).get(key);
  }

  @Override
  public V put(K key, V value) {
    return getTransactionMap(false).put(key, value);
  }

  @Override
  public V remove(Object key) {
    return getTransactionMap(false).remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    getTransactionMap(false).putAll(m);
  }

  @Override
  public void clear() {
    getTransactionMap(false).clear();
  }

  @Override
  public Set<K> keySet() {
    return getTransactionMap(false).keySet();
  }

  @Override
  public Collection<V> values() {
    return getTransactionMap(false).values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return getTransactionMap(false).entrySet();
  }

  @SuppressWarnings("squid:S2160")
  public abstract class AbstractMapTransactionMember extends AbstractMap<K, V> implements ITransactionMember {

    /**
     * Map containing an entry for removes that were done within this transaction, containing the old value from the
     * sharedMap. It contains a <tt>null</tt> entry if there was no previous value in the shared map. Subsequent removes
     * will not change the value in the entry of this map.
     */
    protected final Map<K, V> m_removedMap;
    /**
     * Map containing entry for each inserted value. Before a value is inserted, any previous value has to be removed.
     * In other words, an entry was added to the removedMap.
     */
    protected final Map<K, V> m_insertedMap;
    /**
     * Set contains any entries which where publish by a fast-forward insertion.
     */
    protected final Map<K, V> m_fastForwardMap;

    protected final int m_insertVersion;
    protected final int m_removeVersion;

    private boolean m_insertsDirty;
    private boolean m_removesDirty;

    public AbstractMapTransactionMember(Map<K, V> removedMap, Map<K, V> insertedMap) {
      this(removedMap, insertedMap, AbstractTransactionalMap.this.m_fastForward ? new HashMap<>() : Collections.emptyMap());
    }

    public AbstractMapTransactionMember(Map<K, V> removedMap, Map<K, V> insertedMap, Map<K, V> fastForwardMap) {
      super();
      m_removedMap = removedMap;
      m_insertedMap = insertedMap;
      m_insertVersion = AbstractTransactionalMap.this.m_insertVersion.get();
      m_removeVersion = AbstractTransactionalMap.this.m_removeVersion.get();
      m_fastForwardMap = fastForwardMap;
    }

    /**
     * shared map for read access only
     */
    protected abstract Map<K, V> getReadSharedMap();

    protected Map<K, V> getInsertedMap() {
      return m_insertedMap;
    }

    protected Map<K, V> getRemovedMap() {
      return m_removedMap;
    }

    public void markInsertsDirty() {
      m_insertsDirty = true;
    }

    public void markRemovesDirty() {
      m_removesDirty = true;
    }

    @Override
    public String getMemberId() {
      return m_transactionMemberId;
    }

    @Override
    public boolean needsCommit() {
      return !m_removedMap.isEmpty() || !m_insertedMap.isEmpty() || !m_fastForwardMap.isEmpty() || m_insertsDirty || m_removesDirty;
    }

    @Override
    public boolean commitPhase1() {
      return true;
    }

    protected void commitChanges(Map<K, V> sharedMap) {
      List<K> successfulCommittedChanges = new ArrayList<>();
      List<K> failedCommittedChanges = new ArrayList<>();

      int sharedRemoveVersion;
      if (m_removesDirty) {
        sharedRemoveVersion = AbstractTransactionalMap.this.m_removeVersion.getAndIncrement();
      }
      else {
        sharedRemoveVersion = AbstractTransactionalMap.this.m_removeVersion.get();
      }
      boolean removesValid = sharedRemoveVersion == m_removeVersion;

      int sharedInsertVersion;
      if (m_insertsDirty) {
        sharedInsertVersion = AbstractTransactionalMap.this.m_insertVersion.getAndIncrement();
      }
      else {
        sharedInsertVersion = AbstractTransactionalMap.this.m_insertVersion.get();
      }
      boolean insertsValid = sharedInsertVersion == m_insertVersion;

      if (removesValid) {
        for (Entry<K, V> entry : m_removedMap.entrySet()) {
          K key = entry.getKey();
          V oldValue = entry.getValue();
          V insertedValue = m_insertedMap.remove(key); // note: this map does not contain null-values
          if (insertedValue != null) {
            if (!insertsValid) {
              failedCommittedChanges.add(key);
              continue;
            }
            if (oldValue == null) {
              // there is a remove and an insert operation - if oldValue of remove operation is null, then we cannot detect commit conflicts
              failedCommittedChanges.add(key);
              continue;
            }
            boolean replaceValueSuccess = sharedMap.replace(key, oldValue, insertedValue);
            if (replaceValueSuccess) {
              successfulCommittedChanges.add(key);
            }
            else {
              failedCommittedChanges.add(key);
            }
          }
          else {
            if (oldValue == null) {
              if (sharedMap.containsKey(key)) {
                // remove entry, and there was no previous value in sharedMap but now there is one
                failedCommittedChanges.add(key);
              }
            }
            else {
              if (sharedMap.remove(key, oldValue)) {
                successfulCommittedChanges.add(key);
              }
              else {
                failedCommittedChanges.add(key);
              }
            }
          }
        }
      }
      else {
        for (Entry<K, V> entry : m_removedMap.entrySet()) {
          if (entry.getValue() != null) {
            m_insertedMap.remove(entry.getKey());
          }
        }
        failedCommittedChanges.addAll(m_removedMap.keySet());
      }

      if (insertsValid) {
        for (Entry<K, V> entry : m_insertedMap.entrySet()) {
          K key = entry.getKey();
          V newValue = entry.getValue();
          V previousValue = sharedMap.putIfAbsent(key, newValue);
          if (previousValue != null && !previousValue.equals(newValue)) {
            failedCommittedChanges.add(key);
          }
          else {
            successfulCommittedChanges.add(key);
          }
        }
      }
      else {
        failedCommittedChanges.addAll(m_insertedMap.keySet());
        failedCommittedChanges.addAll(m_fastForwardMap.keySet());
      }

      changesCommitted(sharedMap, successfulCommittedChanges, failedCommittedChanges);
    }

    /**
     * Called after committing any changes into the shared map. In case of concurrent modifications of the same key in
     * different transactions, only one transaction will be able to commit its change. A failed commit must be handled.
     * <p>
     * As a default, entries from the shared map are removed for any failed commit. Therefore, if map is used as a lazy
     * loaded shared cache, the item will be reloaded.
     */
    protected void changesCommitted(Map<K, V> newSharedMap, Collection<K> successfulCommittedChanges, Collection<K> failedCommittedChanges) {
      for (K key : failedCommittedChanges) {
        newSharedMap.remove(key);
      }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void rollback() {
    }

    @Override
    public void release() {
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public V get(Object key) {
      V value = m_insertedMap.get(key);
      if (value == null && !m_removedMap.containsKey(key)) {
        value = getReadSharedMap().get(key);
      }
      return value;
    }

    @Override
    public V put(K key, V value) {
      if (key == null || value == null) {
        // fast fail
        throw new NullPointerException("Null key or value is not allowed in this map implementation"); // NOSONAR (Map API)
      }
      V sharedValue = getReadSharedMap().get(key);
      boolean hasRemoveEntry = m_removedMap.containsKey(key);
      if (m_fastForward && !hasRemoveEntry && sharedValue == null && !m_insertsDirty && fastForward(key, value)) {
        // fastForward success; value was directly put in shared map
        m_fastForwardMap.put(key, value);
        return null;
      }
      V oldValue = m_insertedMap.put(key, value);
      if (!hasRemoveEntry && sharedValue != null) {
        // this may happen even in a lazy cache scenario because of the fastForward feature
        m_removedMap.put(key, sharedValue);
        return sharedValue;
      }
      return oldValue;
    }

    /**
     * Tries to commit a new value directly into the shared map
     *
     * @return true if fast-forward succeeded
     */
    protected boolean fastForward(K key, V value) {
      return false;
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    @Override
    public V remove(Object key) {
      V insertedValue = m_insertedMap.remove(key);
      if (insertedValue != null) {
        return insertedValue;
      }
      else if (m_removedMap.containsKey(key)) {
        return null;
      }
      V oldValue = getReadSharedMap().get(key);
      m_removedMap.put((K) key, oldValue);
      return oldValue;
    }

    @Override
    public void clear() {
      m_removedMap.putAll(getReadSharedMap());
      m_insertedMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
      return m_insertedMap.containsKey(key) || (!m_removedMap.containsKey(key) && getReadSharedMap().containsKey(key));
    }

    @Override
    public int size() {
      if (m_removedMap.containsValue(null)) {
        // If a value in the removed map is null, then there was no entry in the shared map, at the time when remove was called.
        // Now there could be an entry in the shared map. Therefore, we have now to carefully compute the sizes.
        Set<K> keys = new HashSet<>(getReadSharedMap().keySet());
        keys.removeAll(m_removedMap.keySet());
        keys.addAll(m_insertedMap.keySet());
        return keys.size();
      }
      else {
        return getReadSharedMap().size() + m_insertedMap.size() - m_removedMap.size();
      }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return new EntrySet();
    }

    protected Iterator<Entry<K, V>> newEntryIterator() {
      return new EntryIterator();
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return newEntryIterator();
      }

      @Override
      public boolean contains(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> e = (Entry<?, ?>) o;
        V v = AbstractMapTransactionMember.this.get(e.getKey());
        if (v == null) {
          return e.getValue() == null;
        }
        else {
          return v.equals(e.getValue());
        }
      }

      @Override
      public boolean remove(Object o) {
        if (!(o instanceof Entry)) {
          return false;
        }
        Entry<?, ?> e = (Entry<?, ?>) o;
        return AbstractMapTransactionMember.this.remove(e.getKey()) != null;
      }

      @Override
      public int size() {
        return AbstractMapTransactionMember.this.size();
      }

      @Override
      public void clear() {
        AbstractMapTransactionMember.this.clear();
      }
    }

    private final class EntryIterator implements Iterator<Entry<K, V>> {
      private final Map<K, V> m_readSharedMap;
      private final Iterator<Entry<K, V>> m_sharedIterator;
      private final Iterator<Entry<K, V>> m_insertedIterator;
      private Entry<K, V> m_nextEntry;
      private Entry<K, V> m_lastReturned;

      public EntryIterator() {
        m_readSharedMap = getReadSharedMap();
        m_sharedIterator = m_readSharedMap.entrySet().iterator();
        // we must create a copy of the inserted map at iterator construction else
        // ConcurrentModificationException might be thrown
        m_insertedIterator = new HashMap<>(m_insertedMap).entrySet().iterator();
        advance();
      }

      void advance() {
        while (true) {
          if (m_sharedIterator.hasNext()) {
            Entry<K, V> entry = m_sharedIterator.next();
            if (!m_removedMap.containsKey(entry.getKey())) {
              m_nextEntry = new TransactionalWriteEntry(entry.getKey(), entry.getValue());
              break;
            }
            else {
              if (m_insertedMap.containsKey(entry.getKey())) {
                V val = m_insertedMap.get(entry.getKey());
                m_nextEntry = new TransactionalWriteEntry(entry.getKey(), val);
                break;
              }
            }
          }
          else {
            if (m_insertedIterator.hasNext()) {
              Entry<K, V> entry = m_insertedIterator.next();
              if (!m_removedMap.containsKey(entry.getKey()) || !m_readSharedMap.containsKey(entry.getKey())) {
                // else entry already visited
                m_nextEntry = new TransactionalWriteEntry(entry.getKey(), entry.getValue());
                break;
              }
            }
            else {
              m_nextEntry = null;
              break;
            }
          }
        }
      }

      @Override
      public boolean hasNext() {
        return m_nextEntry != null;
      }

      @Override
      public Entry<K, V> next() {
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
        AbstractMapTransactionMember.this.remove(m_lastReturned.getKey());
        m_lastReturned = null;
      }
    }

    private final class TransactionalWriteEntry extends SimpleEntry<K, V> {
      private static final long serialVersionUID = 1L;

      TransactionalWriteEntry(K k, V v) {
        super(k, v);
      }

      @Override
      public V setValue(V value) {
        V v = super.setValue(value);
        AbstractMapTransactionMember.this.put(getKey(), value);
        return v;
      }
    }
  }
}
