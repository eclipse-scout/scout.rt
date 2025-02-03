/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing a dynamic list of synchronizer groups.
 * <p>
 * A group is identified by a key. For each key a lock object is created on first use of the key. Tasks that belong to
 * equal keys are never executed in parallel. Tasks that belong to different keys may run at the same time. Therefore
 * the key object must implement hashCode() & equals() methods.
 * <p>
 * No entries are removed automatically. It is important to remove a group as soon as it is no longer required to
 * prevent memory leaks.
 * <p>
 * This class is thread safe.
 *
 * @since 9.0
 */
public final class GroupedSynchronizer<K, V> {

  public static final int DEFAULT_ROOT_LOCKS = 32;
  private static final Logger LOG = LoggerFactory.getLogger(GroupedSynchronizer.class);

  private final ConcurrentMap<K, V> m_locks;
  private final ReentrantReadWriteLock[] m_rootLocks;

  /**
   * Creates a new instance with {@value #DEFAULT_ROOT_LOCKS} root locks.
   *
   * @see #GroupedSynchronizer(int)
   */
  public GroupedSynchronizer() {
    this(DEFAULT_ROOT_LOCKS);
  }

  /**
   * @param numRootLocks
   *     Number of root lock objects. A higher value uses more memory but has better throughput as exclusive remove
   *     operations block less concurrent read operations. It is recommended to use a power of 2.
   * @see #GroupedSynchronizer(int, boolean)
   */
  public GroupedSynchronizer(int numRootLocks) {
    this(numRootLocks, false);
  }

  /**
   * @param fair
   *     {@code true} if a root lock should use a fair ordering policy. For more details on fair mode versus
   *     non-fair mode see {@link ReentrantReadWriteLock}.
   */
  public GroupedSynchronizer(boolean fair) {
    this(DEFAULT_ROOT_LOCKS, fair);
  }

  /**
   * @param numRootLocks
   *     Number of root lock objects. A higher value uses more memory but has better throughput as exclusive remove
   *     operations block less concurrent read operations. It is recommended to use a power of 2.
   * @param fair
   *     {@code true} if a root lock should use a fair ordering policy. For more details on fair mode versus
   *     non-fair mode see {@link ReentrantReadWriteLock}.
   */
  public GroupedSynchronizer(int numRootLocks, boolean fair) {
    m_locks = new ConcurrentHashMap<>();
    m_rootLocks = new ReentrantReadWriteLock[numRootLocks];
    for (int i = 0; i < m_rootLocks.length; i++) {
      m_rootLocks[i] = new ReentrantReadWriteLock(fair);
    }
  }

  /**
   * Runs the given task {@link Runnable} within a lock that exists for each groupKey provided. No tasks with the same
   * groupKey run in parallel.
   * <p>
   * More formally:<br>
   * Acquires a global lock to obtain the group specific lock and then executes the given task {@link Runnable} within
   * the group lock. The global lock is released as soon as the group lock has been acquired. A group entry cannot be
   * removed while a task using the same entry is running.
   *
   * @param groupKey
   *     The key object that identifies the group. Must not be {@code null}. The given class must implement
   *     {@link #equals(Object)} and {@link #hashCode()}.
   * @param task
   *     The task to execute under the group lock. Must not be {@code null}.
   * @param lockFactory
   *     A {@link Function} invoked if a new lock group should be created for a key. The input of the function is
   *     the key for which a new lock object should be created. The return value will be used as locking object and
   *     must not be {@code null}! This function may not be {@code null}.
   * @throws RuntimeException
   *     occurred while executing the given task or acquiring the group lock.
   */
  public void runInGroupLock(K groupKey, Runnable task, Function<? super K, ? extends V> lockFactory) {
    acceptInGroupLock(groupKey, lockObj -> task.run(), lockFactory);
  }

  /**
   * Accepts the given task {@link Consumer} within a lock that exists for each groupKey provided. No tasks with the
   * same groupKey run in parallel.
   * <p>
   * More formally:<br>
   * Acquires a global lock to obtain the group specific lock and then executes the given task {@link Consumer} within
   * the group lock. The global lock is released as soon as the group lock has been acquired. A group entry cannot be
   * removed while a task using the same entry is running.
   *
   * @param groupKey
   *     The key object that identifies the group. Must not be {@code null}. The given class must implement
   *     {@link #equals(Object)} and {@link #hashCode()}.
   * @param task
   *     The task to execute under the group lock. Must not be {@code null}. The instance given to the task is the
   *     group lock instance under which the task is executed. This group lock instance is never {@code null}.
   * @param lockFactory
   *     A {@link Function} invoked if a new lock group should be created for a key. The input of the function is
   *     the key for which a new lock object should be created. The return value will be used as locking object and
   *     must not be {@code null}! This function may not be {@code null}.
   * @throws RuntimeException
   *     occurred while executing the given task or acquiring the group lock.
   */
  public void acceptInGroupLock(K groupKey, Consumer<? super V> task, Function<? super K, ? extends V> lockFactory) {
    applyInGroupLock(groupKey, lockObj -> {
      task.accept(lockObj);
      return null;
    }, lockFactory);
  }

  /**
   * Applies the given task {@link Function} within a lock that exists for each groupKey provided. No tasks with the
   * same groupKey run in parallel.
   * <p>
   * More formally:<br>
   * Acquires a global lock to obtain the group specific lock and then executes the given task {@link Function} within
   * the group lock. The global lock is released as soon as the group lock has been acquired. A group entry cannot be
   * removed while a task using the same entry is running.
   *
   * @param groupKey
   *     The key object that identifies the group. Must not be {@code null}. The given class must implement
   *     {@link #equals(Object)} and {@link #hashCode()}.
   * @param task
   *     The task to execute under the group lock. Must not be {@code null}. The instance given to the task is the
   *     group lock instance under which the task is executed. This group lock instance is never {@code null}.
   * @param lockFactory
   *     A {@link Function} invoked if a new lock group should be created for a key. The input of the function is
   *     the key for which a new lock object should be created. The return value will be used as locking object and
   *     must not be {@code null}! This function may not be {@code null}.
   * @return The result of the executed task function.
   * @throws RuntimeException
   *     occurred while executing the given task or acquiring the group lock.
   */
  public <R> R applyInGroupLock(K groupKey, Function<? super V, ? extends R> task, Function<? super K, ? extends V> lockFactory) {
    assertNotNull(groupKey, "key may not be null");
    assertNotNull(task, "task may not be null");
    assertNotNull(lockFactory, "lockFactory may not be null");

    final ReentrantReadWriteLock rwl = lockFor(groupKey);
    final ReadLock rl = rwl.readLock();
    rl.lock();
    try {
      final V lockGroup = computeLockGroupIfAbsent(groupKey, lockFactory);
      return runTaskInGroupLock(lockGroup, task, rl);
    }
    finally {
      if (rwl.getReadHoldCount() > 0) {
        rl.unlock();
      }
    }
  }

  V computeLockGroupIfAbsent(K groupKey, Function<? super K, ? extends V> lockFactory) {
    return m_locks.computeIfAbsent(groupKey, lockFactory);
  }

  <R> R runTaskInGroupLock(V lockGroup, Function<? super V, ? extends R> task, ReadLock rootLock) {
    synchronized (lockGroup) {
      // release global lock. after that only the group lock is active which does not longer block other clients from acquiring group locks.
      // the task is executed in the group lock without affecting other clients but still blocking requests from equal clients.
      rootLock.unlock();
      return task.apply(lockGroup);
    }
  }

  ReentrantReadWriteLock lockFor(K key) {
    int hash = Objects.hashCode(key) & Integer.MAX_VALUE;
    int bucket = hash % numRootLocks();
    return m_rootLocks[bucket];
  }

  /**
   * Removes the group having the given group key from this {@link GroupedSynchronizer}.
   * <p>
   * <b>Important:</b><br>
   * If the group is currently used by a task running under that group lock, this method blocks until the group is ready
   * to be removed. This also means that within a group lock task this remove method must not be called (possibility for
   * deadlock)!
   *
   * @param groupKey
   *     The key object that identifies the group. Must not be {@code null}.
   * @return The removed lock object or {@code null} if no group could be found having the given group key.
   */
  public V remove(K groupKey) {
    return remove(groupKey, null);
  }

  /**
   * Removes the group having the given group key from this {@link GroupedSynchronizer}.
   * <p>
   * <b>Important:</b><br>
   * If the group is currently used by a task running under that group lock, this method blocks until the group is ready
   * to be removed. This also means that within a group lock task this remove method must not be called (possibility for
   * deadlock)!
   *
   * @param groupKey
   *     The key object that identifies the group. Must not be {@code null}.
   * @param shouldRemove
   *     An optional {@link Predicate} that is executed while holding the group lock but before the group is being
   *     removed. This callback may be used to perform an atomic check if the remove should really be executed. If
   *     the {@link Predicate} returns {@code false}, nothing will be removed and this method call is a noop. If no
   *     {@link Predicate} is present or the {@link Predicate} returns {@code true}, the group will be removed.
   * @return The removed lock object or {@code null} if nothing was removed (because the key was not found or the
   * callback returned {@code false}).
   */
  public V remove(K groupKey, Predicate<? super V> shouldRemove) {
    assertNotNull(groupKey, "key may not be null");

    final WriteLock rootLock = lockFor(groupKey).writeLock();
    rootLock.lock();
    try {
      return removeEntry(groupKey, shouldRemove);
    }
    finally {
      rootLock.unlock();
    }
  }

  V removeEntry(K groupKey, Predicate<? super V> shouldRemove) {
    final V lockGroup = m_locks.get(groupKey);
    if (lockGroup == null) {
      return null;
    }

    synchronized (lockGroup) {
      if (accept(lockGroup, shouldRemove)) {
        return m_locks.remove(groupKey);
      }
    }
    return null;
  }

  boolean accept(V element, Predicate<? super V> predicate) {
    if (predicate == null) {
      return true;
    }

    try {
      return predicate.test(element);
    }
    catch (RuntimeException e) {
      LOG.warn("Error evaluating lock predicate", e);
      return true; // to ensure there are no leaks in the map
    }
  }

  /**
   * @return A read only live {@link Map} view on this {@link GroupedSynchronizer}. The map key is the group key and the
   * map value the corresponding group lock object.
   */
  public Map<K, V> toMap() {
    return Collections.unmodifiableMap(m_locks);
  }

  /**
   * @return The number of currently locked root locks. This means this number of root locks cannot be used to acquire a
   * group lock and task execution for those root locks is delayed.
   */
  public int numLockedRootLocks() {
    return (int) Stream.of(m_rootLocks)
        .filter(ReentrantReadWriteLock::isWriteLocked)
        .count();
  }

  /**
   * @return The total number of root lock objects
   */
  public int numRootLocks() {
    return m_rootLocks.length;
  }

  /**
   * @return The number of groups in this synchronizer.
   */
  public int size() {
    return m_locks.size();
  }
}
