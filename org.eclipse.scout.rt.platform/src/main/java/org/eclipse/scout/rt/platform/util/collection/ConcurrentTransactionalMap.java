/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.transaction.ITransactionMember;

/**
 * Uses a {@link ConcurrentHashMap} as a shared thread-safe map. Use this class as a default implementation for
 * {@link AbstractTransactionalMap}.
 *
 * @see AbstractTransactionalMap
 * @since 5.2
 */
@SuppressWarnings("squid:S2160")
public class ConcurrentTransactionalMap<K, V> extends AbstractTransactionalMap<K, V> implements ConcurrentMap<K, V> {
  private final ConcurrentMap<K, V> m_sharedMap;

  public ConcurrentTransactionalMap(String transactionMemberId) {
    this(transactionMemberId, true);
  }

  public ConcurrentTransactionalMap(String transactionMemberId, boolean fastForward) {
    super(transactionMemberId, fastForward);
    m_sharedMap = new ConcurrentHashMap<>();
  }

  public ConcurrentTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> m) {
    super(transactionMemberId, fastForward);
    m_sharedMap = new ConcurrentHashMap<>(m);
  }

  @Override
  protected ConcurrentMap<K, V> getSharedMap() {
    return m_sharedMap;
  }

  @Override
  public V putIfAbsent(K key, V value) {
    return getTransactionMap(false).putIfAbsent(key, value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return getTransactionMap(false).remove(key, value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    return getTransactionMap(false).replace(key, oldValue, newValue);
  }

  @Override
  public V replace(K key, V value) {
    return getTransactionMap(false).replace(key, value);
  }

  @Override
  protected ConcurrentMap<K, V> getTransactionMap(boolean onlyReadOperation) {
    return (ConcurrentMap<K, V>) super.getTransactionMap(onlyReadOperation);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <TM extends Map<K, V> & ITransactionMember> TM createMapTransactionMember() {
    return (TM) new ConcurrentMapTransactionMember(true);
  }

  public class ConcurrentMapTransactionMember extends AbstractMapTransactionMember implements ConcurrentMap<K, V> {

    /**
     * shared map for read access only
     */
    protected final Map<K, V> m_readSharedMap;

    public ConcurrentMapTransactionMember(boolean sharedRead) {
      this(sharedRead ? m_sharedMap : new HashMap<>(m_sharedMap), new HashMap<>(), new HashMap<>());
    }

    public ConcurrentMapTransactionMember(Map<K, V> readSharedMap, Map<K, V> removedMap, Map<K, V> insertedMap) {
      super(removedMap, insertedMap);
      m_readSharedMap = readSharedMap;
    }

    protected ConcurrentMap<K, V> getSharedMap() {
      return ConcurrentTransactionalMap.this.m_sharedMap;
    }

    @Override
    protected Map<K, V> getReadSharedMap() {
      return m_readSharedMap;
    }

    @Override
    public void commitPhase2() {
      commitChanges(getSharedMap());
    }

    @Override
    protected boolean fastForward(K key, V value) {
      return getSharedMap().putIfAbsent(key, value) == null;
    }

    // The additional concurrent methods below can be implemented straight forward as each TransactionMember
    // is only accessible in one thread. Note, that we do not have to use containsKey as this map does not allow
    // null values.

    @Override
    public V putIfAbsent(K key, V value) {
      V v = get(key);
      if (v == null) {
        v = put(key, value);
      }

      return v;
    }

    @Override
    public boolean remove(Object key, Object value) {
      Object curValue = get(key);
      if (!Objects.equals(curValue, value) || curValue == null) {
        return false;
      }
      remove(key);
      return true;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      Object curValue = get(key);
      if (!Objects.equals(curValue, oldValue) || curValue == null) {
        return false;
      }
      put(key, newValue);
      return true;
    }

    @Override
    public V replace(K key, V value) {
      V curValue;
      if (((curValue = get(key)) != null) || containsKey(key)) {
        curValue = put(key, value);
      }
      return curValue;
    }
  }
}
