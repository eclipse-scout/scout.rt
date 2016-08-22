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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.transaction.ITransactionMember;

/**
 * Uses a copy-on-write technique in order to ensure a thread-safe behavior. This map may be faster than
 * {@link ConcurrentTransactionalMap} if the map has a small fixed size or <tt>fastForward</tt> is not used.
 * <p>
 * Unlike {@link ConcurrentTransactionalMap}, if this map is used outside a transaction, the map is <em>not</em>
 * modifiable.
 *
 * @see AbstractTransactionalMap
 * @since 5.2
 */
public class CopyOnWriteTransactionalMap<K, V> extends AbstractTransactionalMap<K, V> {
  private final Object m_sharedMapLock = new Object();

  /**
   * Variable is never null and no modifications on this map are allowed. Internally, this map may or <b>may not</b> be
   * wrapped with {@link Collections#unmodifiableMap(Map)} for performance reason.
   */
  private volatile Map<K, V> m_sharedMap;

  public CopyOnWriteTransactionalMap(String transactionMemberId) {
    this(transactionMemberId, true);
  }

  public CopyOnWriteTransactionalMap(String transactionMemberId, boolean fastForward) {
    super(transactionMemberId, fastForward);
    m_sharedMap = Collections.emptyMap();
  }

  public CopyOnWriteTransactionalMap(String transactionMemberId, boolean fastForward, Map<K, V> m) {
    super(transactionMemberId, fastForward);
    m_sharedMap = m;
  }

  @Override
  protected Map<K, V> getSharedMap() {
    // no transaction found, therefore access to shared map
    // return read only map to prevent concurrent modifications
    return Collections.unmodifiableMap(m_sharedMap);
  }

  protected Map<K, V> getSharedMapInternal() {
    return m_sharedMap;
  }

  protected Object getSharedMapLock() {
    return m_sharedMapLock;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <TM extends Map<K, V> & ITransactionMember> TM createMapTransactionMember() {
    return (TM) new CopyOnWriteMapTransactionMember(getTransactionMemberId(), isFastForward());
  }

  public class CopyOnWriteMapTransactionMember extends AbstractTransactionalMap.AbstractMapTransactionMember<K, V> {

    public CopyOnWriteMapTransactionMember(String transactionMemberId, boolean fastForward) {
      this(transactionMemberId, new HashMap<K, V>(), new HashMap<K, V>(), fastForward);
    }

    public CopyOnWriteMapTransactionMember(String transactionId, Map<K, V> removedMap, Map<K, V> insertedMap, boolean fastForward) {
      super(transactionId, removedMap, insertedMap, fastForward);
    }

    @Override
    protected Map<K, V> getReadSharedMap() {
      return m_sharedMap;
    }

    @Override
    public void commitPhase2() {
      synchronized (m_sharedMapLock) {
        HashMap<K, V> newSharedMap = new HashMap<>(m_sharedMap);
        Collection<K> successfulCommitedChanges = new ArrayList<>();
        Collection<K> failedCommitedChanges = new ArrayList<>();
        for (Entry<K, V> entry : getRemovedMap().entrySet()) {
          K key = entry.getKey();
          V oldValue = entry.getValue();
          if (oldValue != null) {
            V insertedValue = getInsertedMap().remove(key);
            if (insertedValue != null) {
              if (oldValue.equals(m_sharedMap.get(key))) {
                newSharedMap.put(key, insertedValue);
                successfulCommitedChanges.add(key);
              }
              else {
                failedCommitedChanges.add(key);
              }
            }
            else {
              if (oldValue.equals(m_sharedMap.get(key))) {
                newSharedMap.remove(key);
                successfulCommitedChanges.add(key);
              }
              else {
                failedCommitedChanges.add(key);
              }
            }
          }
          else {
            // if there must be a value inserted, the loop over insertedMap will handle this
            if (m_sharedMap.containsKey(key)) {
              // remove entry, and there was no previous value in sharedMap but now there is one. Commit failed.
              failedCommitedChanges.add(key);
            }
          }
        }
        for (Entry<K, V> entry : getInsertedMap().entrySet()) {
          K key = entry.getKey();
          V newValue = entry.getValue();
          V previousValue = m_sharedMap.get(key);
          if (previousValue != null && !previousValue.equals(newValue)) {
            failedCommitedChanges.add(key);
          }
          else {
            newSharedMap.put(key, newValue);
            successfulCommitedChanges.add(key);
          }
        }
        changesCommited(newSharedMap, successfulCommitedChanges, failedCommitedChanges);
        m_sharedMap = newSharedMap;
      }
    }

    @Override
    protected boolean fastForward(K key, V value) {
      synchronized (m_sharedMapLock) {
        if (m_sharedMap.get(key) == null) {
          HashMap<K, V> newSharedMap = new HashMap<>(m_sharedMap);
          newSharedMap.put(key, value);
          m_sharedMap = newSharedMap;
          return true;
        }
      }
      return false;
    }
  }
}
