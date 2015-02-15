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
package org.eclipse.scout.commons.job.interceptor;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;

/**
 * Processor to bind a value to the thread's {@link ThreadLocal} during the time of executing a job.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <R>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ThreadLocalInitializer<R, T> implements Callable<R>, Chainable {

  private final Callable<R> m_next;
  private final ThreadLocal<T> m_threadLocal;
  private final T m_value;

  /**
   * Creates a processor to set the given {@link ThreadLocal} during the time of execution.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param threadLocal
   *          {@link ThreadLocal} to bind the given value to; must not be <code>null</code>.
   * @param value
   *          value to be bound.
   */
  public ThreadLocalInitializer(final Callable<R> next, final ThreadLocal<T> threadLocal, final T value) {
    m_next = Assertions.assertNotNull(next);
    m_threadLocal = Assertions.assertNotNull(threadLocal);
    m_value = value;
  }

  @Override
  public R call() throws Exception {
    final T oldValue = m_threadLocal.get();

    m_threadLocal.set(m_value);
    try {
      return m_next.call();
    }
    finally {
      if (oldValue == null) {
        m_threadLocal.remove();
      }
      else {
        m_threadLocal.set(oldValue);
      }
    }
  }

  @Override
  public Callable<R> getNext() {
    return m_next;
  }

  public ThreadLocal<T> getThreadLocal() {
    return m_threadLocal;
  }
}
