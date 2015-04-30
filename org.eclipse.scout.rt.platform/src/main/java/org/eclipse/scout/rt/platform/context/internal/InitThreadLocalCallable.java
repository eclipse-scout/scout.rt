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
package org.eclipse.scout.rt.platform.context.internal;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IChainable;

/**
 * Processor to bind a value to the thread's {@link ThreadLocal} during the time of executing a job.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @param <THREAD_LOCAL>
 *          the type of the {@link ThreadLocal}.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class InitThreadLocalCallable<RESULT, THREAD_LOCAL> implements ICallable<RESULT>, IChainable<ICallable<RESULT>> {

  protected final ICallable<RESULT> m_next;
  protected final ThreadLocal<THREAD_LOCAL> m_threadLocal;
  protected final THREAD_LOCAL m_value;

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
  public InitThreadLocalCallable(final ICallable<RESULT> next, final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    m_next = Assertions.assertNotNull(next);
    m_threadLocal = Assertions.assertNotNull(threadLocal);
    m_value = value;
  }

  @Override
  public RESULT call() throws Exception {
    final THREAD_LOCAL oldValue = m_threadLocal.get();

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

  public ThreadLocal<THREAD_LOCAL> getThreadLocal() {
    return m_threadLocal;
  }

  @Override
  public ICallable<RESULT> getNext() {
    return m_next;
  }
}
