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

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * An effectively final value that can be lazily set.
 *
 * @since 5.1
 */
public class FinalValue<VALUE> {

  private final Object m_lock = new Object();

  private volatile VALUE m_value;
  private volatile boolean m_set = false;

  /**
   * Create without initial value
   */
  public FinalValue() {
  }

  /**
   * Create with initial value
   */
  public FinalValue(final VALUE value) {
    set(value);
  }

  /**
   * @return the value or <code>null</code>, if not initialized.
   */
  public VALUE get() {
    return m_value;
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
    synchronized (m_lock) {
      Assertions.assertFalse(m_set, "{} already set.", getClass().getSimpleName());
      setIfAbsent(value);
    }
  }

  /**
   * Sets the specified value as final value, but only if not set yet.
   *
   * @return the final value.
   */
  public VALUE setIfAbsent(final VALUE value) {
    return setIfAbsent(new Callable<VALUE>() {
      @Override
      public VALUE call() {
        return value;
      }
    });
  }

  /**
   * Computes the final value with the specified producer, but only if not set yet.
   *
   * @param producer
   *          to produce the final value if no final value is set yet.
   * @return the final value.
   * @throws RuntimeException
   *           if the producer throws an exception
   */
  public VALUE setIfAbsent(final Callable<VALUE> producer) {
    if (m_set) {
      return m_value;
    }

    synchronized (m_lock) {
      // double-checked locking
      if (m_set) {
        return m_value;
      }

      try {
        m_value = producer.call();
        m_set = true;
      }
      catch (final RuntimeException | Error e) {
        throw e;
      }
      catch (final Exception e) {
        throw new PlatformException("Failed to produce final value", e);
      }
      return m_value;
    }
  }

  /**
   * @return <code>true</code>, if a final value was set, or else <code>false</code>.
   */
  public boolean isSet() {
    return m_set;
  }
}
