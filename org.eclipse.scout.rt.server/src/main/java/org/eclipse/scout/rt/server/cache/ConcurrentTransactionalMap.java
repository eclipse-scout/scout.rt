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
package org.eclipse.scout.rt.server.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    m_sharedMap = new ConcurrentHashMap<K, V>();
  }

  public ConcurrentTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> m) {
    super(transactionMemberId, fastForward);
    m_sharedMap = new ConcurrentHashMap<K, V>(m);
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
    return (TM) new ConcurrentMapTransactionMember<K, V>(getTransactionMemberId(), getSharedMap(), true, isFastForward());
  }

  public static class ConcurrentMapTransactionMember<K, V> extends AbstractTransactionalMap.AbstractMapTransactionMember<K, V> implements ConcurrentMap<K, V> {

    /**
     * shared map over all transactions
     */
    protected final ConcurrentMap<K, V> m_sharedMap;

    /**
     * shared map for read access only
     */
    protected final Map<K, V> m_readSharedMap;

    public ConcurrentMapTransactionMember(String transactionMemberId, ConcurrentMap<K, V> sharedMap, boolean sharedRead, boolean fastForward) {
      // If reads should not be shared, we create a full copy of the shared map
      this(transactionMemberId, sharedMap, sharedRead ? sharedMap : new HashMap<K, V>(sharedMap), fastForward);
    }

    public ConcurrentMapTransactionMember(String transactionMemberId, ConcurrentMap<K, V> sharedMap, Map<K, V> readSharedMap, boolean fastForward) {
      this(transactionMemberId, sharedMap, readSharedMap, new HashMap<K, V>(), new HashMap<K, V>(), fastForward);
    }

    public ConcurrentMapTransactionMember(String transactionId, ConcurrentMap<K, V> sharedMap, Map<K, V> readSharedMap, Map<K, V> removedMap, Map<K, V> insertedMap, boolean fastForward) {
      super(transactionId, removedMap, insertedMap, fastForward);
      m_sharedMap = sharedMap;
      m_readSharedMap = readSharedMap;
    }

    protected ConcurrentMap<K, V> getSharedMap() {
      return m_sharedMap;
    }

    @Override
    protected Map<K, V> getReadSharedMap() {
      return m_readSharedMap;
    }

    @Override
    public void commitPhase2() {
      ConcurrentMap<K, V> sharedMap = getSharedMap();
      Collection<K> successfulCommitedChanges = new ArrayList<K>();
      Collection<K> failedCommitedChanges = new ArrayList<K>();
      for (Entry<K, V> entry : getRemovedMap().entrySet()) {
        K key = entry.getKey();
        V oldValue = entry.getValue();
        if (oldValue != null) {
          V insertedValue = getInsertedMap().remove(key);
          if (insertedValue != null) {
            if (sharedMap.replace(key, oldValue, insertedValue)) {
              successfulCommitedChanges.add(key);
            }
            else {
              failedCommitedChanges.add(key);
            }
          }
          else {
            if (sharedMap.remove(key, oldValue)) {
              successfulCommitedChanges.add(key);
            }
            else {
              failedCommitedChanges.add(key);
            }
          }
        }
        else {
          // if there must be a value inserted, the loop over insertedMap will handle this
          if (sharedMap.containsKey(key)) {
            // remove entry, and there was no previous value in sharedMap but now there is one. Commit failed.
            failedCommitedChanges.add(key);
          }
        }
      }
      for (Entry<K, V> entry : getInsertedMap().entrySet()) {
        K key = entry.getKey();
        V newValue = entry.getValue();
        V previousValue = sharedMap.putIfAbsent(key, newValue);
        if (previousValue != null && !previousValue.equals(newValue)) {
          failedCommitedChanges.add(key);
        }
        else {
          successfulCommitedChanges.add(key);
        }
      }
      changesCommited(sharedMap, successfulCommitedChanges, failedCommitedChanges);
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
      V currentValue = get(key);
      if (currentValue == null) {
        return put(key, value);
      }
      else {
        return currentValue;
      }
    }

    @Override
    public boolean remove(Object key, Object value) {
      V currentValue = get(key);
      if (currentValue != null && currentValue.equals(value)) {
        remove(key);
        return true;
      }
      else {
        return false;
      }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      V currentValue = get(key);
      if (currentValue != null && currentValue.equals(oldValue)) {
        put(key, newValue);
        return true;
      }
      else {
        return false;
      }
    }

    @Override
    public V replace(K key, V value) {
      if (containsKey(key)) {
        return put(key, value);
      }
      else {
        return null;
      }
    }
  }
}
