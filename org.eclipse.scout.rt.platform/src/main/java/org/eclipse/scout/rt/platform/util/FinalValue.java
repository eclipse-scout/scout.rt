/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * An effectively final value that can be lazily initialized.
 *
 * @since 5.1
 */
public class FinalValue<VALUE> {

  private volatile VALUE m_value;
  private final Object m_lock = new Object();
  private volatile boolean m_initialized = false;

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
   * Sets the value, if it is not initialized yet. Throws {@link AssertionException} otherwise.
   *
   * @param value
   *          value to set
   */
  public void set(final VALUE value) {
    synchronized (m_lock) {
      Assertions.assertFalse(m_initialized, "%s can only be set once.", getClass().getSimpleName());
      setIfAbsent(value);
    }
  }

  /**
   * Sets the value, if it is not already initialized.
   *
   * @return the current value.
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
   * Compute the value with the producer, if it is not already initialized
   *
   * @param producer
   *          to create a value, if not initialized
   * @return the current value.
   * @throws RuntimeException
   *           if the producer throws an exception
   */
  public VALUE setIfAbsent(final Callable<VALUE> producer) {
    if (m_initialized) {
      return m_value;
    }

    synchronized (m_lock) {
      if (m_value == null) {
        try {
          m_value = producer.call();
          m_initialized = true;
        }
        catch (final RuntimeException e) {
          throw e;
        }
        catch (final Error e) {
          throw e;
        }
        catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }
      return m_value;
    }
  }

  /**
   * @return <code>true</code>, if it has been initialized.
   */
  public boolean isInitialized() {
    return m_initialized;
  }
}
