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
package org.eclipse.scout.rt.platform.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * An effectively final value that can be lazily set.
 *
 * @since 5.1
 */
public class FinalValue<VALUE> {

  /**
   * Marker for initial null-value. It prevents an additional (volatile) boolean member that would track if the final
   * value has already been set.
   */
  private static final Object NULL_VALUE = new Object();

  private final AtomicReference<Object> m_value = new AtomicReference<>(NULL_VALUE);

  /**
   * @return the value or <code>null</code>, if not set.
   */
  @SuppressWarnings("unchecked")
  public VALUE get() {
    Object value = m_value.get();
    return value == NULL_VALUE ? null : (VALUE) value;
  }

  /**
   * Sets the specified value as final value, or throws {@link AssertionException} if already set.
   *
   * @param value
   *          value to set
   * @throws AssertionException
   *           if a final value is already set.
   */
  public void set(final VALUE value) {
    Assertions.assertSame(m_value.get(), NULL_VALUE, "{} already set.", getClass().getSimpleName());
    setIfAbsent(value);
  }

  /**
   * Sets the specified value as final value, but only if not set yet.
   *
   * @return the final value.
   */
  public VALUE setIfAbsentAndGet(final VALUE value) {
    return setIfAbsentAndGet(() -> value);
  }

  /**
   * Computes the final value with the specified producer, but only if not set yet. It makes the same promises as other
   * concurrent structures (e.g. like {@link ConcurrentMap}): the producer could be executed concurrently by multiple
   * threads but only first available value is used. The producer is responsible for dealing with this fact.
   *
   * @param producer
   *          to produce the final value if no final value is set yet.
   * @return the final value.
   * @throws RuntimeException
   *           if the producer throws an exception
   */
  @SuppressWarnings("unchecked")
  public VALUE setIfAbsentAndGet(final Callable<VALUE> producer) {
    setIfAbsent(producer);
    return (VALUE) m_value.get();
  }

  /**
   * Sets the specified value as final value, but only if not set yet.
   *
   * @return <code>true</code>, if the value was set with the given producer, <code>false</code>, if a value already
   *         exited.
   */
  public boolean setIfAbsent(final VALUE value) {
    return setIfAbsent(() -> value);
  }

  /**
   * Computes the final value with the specified producer, but only if not set yet. It makes the same promises as other
   * concurrent structures (e.g. like {@link ConcurrentMap}): the producer could be executed concurrently by multiple
   * threads but only first available value is used. The producer is responsible for dealing with this fact.
   *
   * @param producer
   *          to produce the final value if no final value is set yet.
   * @return <code>true</code>, if the value was set with the given producer, <code>false</code>, if a value already
   *         exited.
   * @throws RuntimeException
   *           if the producer throws an exception
   */
  public boolean setIfAbsent(final Callable<VALUE> producer) {
    Object value = m_value.get();
    if (value != NULL_VALUE) {
      return false;
    }

    try {
      return m_value.compareAndSet(NULL_VALUE, producer.call());
    }
    catch (final RuntimeException e) {
      throw e;
    }
    catch (final Exception e) {
      throw new PlatformException("Failed to produce final value", e);
    }
  }

  /**
   * @return <code>true</code>, if a final value was set, or else <code>false</code>.
   */
  public boolean isSet() {
    return m_value.get() != NULL_VALUE;
  }
}
