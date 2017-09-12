/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer.pool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Non-blocking, unlimited pool. Elements are removed after a given timeout.
 *
 * @param <T>
 *          type of pooled elements.
 * @param <P>
 *          type of additional parameters used for creating a new pool element.
 * @since 6.0.300
 */
public abstract class AbstractNonBlockingPool<T> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractNonBlockingPool.class);

  private final long m_maxAgeMillis;
  private final AtomicLong m_poolSize = new AtomicLong();

  private final ConcurrentMap<T, State> m_idleElements = new ConcurrentHashMap<>();
  private final ConcurrentMap<T, State> m_busyElements = new ConcurrentHashMap<>();

  public AbstractNonBlockingPool(long maxAge, TimeUnit timeUnit) {
    m_maxAgeMillis = timeUnit.toMillis(maxAge);
  }

  public T lease() {
    for (T candidate : m_idleElements.keySet()) {
      final State state = m_idleElements.remove(candidate);
      if (state == null) {
        // Another thread took the element concurrently.
        continue;
      }

      if (state.isExpired()) {
        // The element reached its max age.
        cleanupInternal(candidate);
        continue;
      }

      state.incUsageCount();
      m_busyElements.put(candidate, state);
      return candidate;
    }

    // no element available - create new element, increasing the pool size
    final T result = createElement();
    m_poolSize.incrementAndGet();
    m_busyElements.put(result, new State(System.currentTimeMillis() + m_maxAgeMillis));
    return result;
  }

  /**
   * Releases the given element (i.e. puts it back into the pool).
   */
  public void release(T o) {
    boolean recycle = false;
    try {
      recycle = resetElement(o);
    }
    catch (RuntimeException e) {
      LOG.warn("Could not reset pooled element. It is not recyceled.", LOG.isDebugEnabled() ? e : null);
    }
    release(o, recycle);
  }

  protected void release(T o, boolean recycle) {
    final State state = m_busyElements.remove(o);
    if (state == null) {
      // element was not managed by this pool. Hence do not invoke cleanupInternal
      cleanup(o);
    }
    else if (state.isExpired() || !recycle) {
      cleanupInternal(o);
    }
    else {
      m_idleElements.put(o, state);
    }
  }

  protected abstract T createElement();

  /**
   * Resets the given pool element before it is put back. Implementers must return <code>true</code> if the element can
   * be recycled. If the method returns <code>false</code> or throws a {@link RuntimeException}, the element is
   * cleaned-up. This method is invoked by {@link #release(Object)} and the return value is used to invoke the protected
   * method {@link #release(Object, boolean)}.
   */
  protected abstract boolean resetElement(T element);

  private void cleanupInternal(T o) {
    try {
      cleanup(o);
    }
    finally {
      m_poolSize.decrementAndGet();
    }
  }

  /**
   * This method is called when an element is removed from the pool because it is expired. Override it to free up
   * resources held by the element, if any.
   */
  protected void cleanup(T o) {
  }

  /**
   * Discards expired pool entries.
   */
  public void discardExpiredPoolEntries() {
    discardPoolEntries(false);
  }

  /**
   * Discards all pool entries.
   */
  public void discardAllPoolEntries() {
    discardPoolEntries(true);
  }

  /**
   * Discards pool entries.
   *
   * @param all
   *          discards all entries if parameter value is <code>true</code>. Otherwise only expired ones.
   */
  protected void discardPoolEntries(boolean all) {
    try {
      for (T idleElement : new HashSet<>(m_idleElements.keySet())) {
        final State state = m_idleElements.remove(idleElement);
        if (state == null) {
          // Another thread took the element concurrently.
          continue;
        }
        if (state.isExpired() || all) {
          cleanupInternal(idleElement);
          continue;
        }

        // Element is still valid. Put it back.
        m_idleElements.put(idleElement, state);
      }
    }
    catch (Exception e) {
      LOG.warn("Exception while managing pool", e);
    }
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  public String createStateSnapshot() {
    final int busySize = m_busyElements.size();
    final int idleSize = m_idleElements.size();
    final long poolSize = m_poolSize.get();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s - pooling %d elements (%d busy, %d idle, %d being created, recycled or destroyed), maxAge %dms<br>",
        getClass().getSimpleName(), poolSize, busySize, idleSize, poolSize - busySize - idleSize, m_maxAgeMillis));

    for (Entry<T, State> e : m_busyElements.entrySet()) {
      sb.append("&nbsp;Busy: ").append(e.getValue()).append(", ").append(createElementStateSnapshot(e.getKey())).append("<br>");
    }

    for (Entry<T, State> e : m_idleElements.entrySet()) {
      sb.append("&nbsp;Idle: ").append(e.getValue()).append(", ").append(createElementStateSnapshot(e.getKey())).append("<br>");
    }
    return sb.toString();
  }

  protected String createElementStateSnapshot(T o) {
    if (o == null) {
      return "[null]";
    }
    return o.toString();
  }

  private static final class State {

    private final long m_expiry;
    private long m_usageCount;

    private State(long expiry) {
      m_expiry = expiry;
      m_usageCount = 1;
    }

    public boolean isExpired() {
      return m_expiry < System.currentTimeMillis();
    }

    public void incUsageCount() {
      m_usageCount++;
    }

    @Override
    public String toString() {
      return "State [m_expiry=" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS", Locale.US).format(new Date(m_expiry)) + ", m_usageCount=" + m_usageCount + "]";
    }
  }
}
