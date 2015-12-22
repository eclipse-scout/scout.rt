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

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;

/**
 * Processor to set a thread-local variable for the subsequent sequence of actions.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class ThreadLocalProcessor<THREAD_LOCAL> implements ICallableDecorator {

  protected final ThreadLocal<THREAD_LOCAL> m_threadLocal;
  protected final THREAD_LOCAL m_value;

  public ThreadLocalProcessor(final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    m_threadLocal = Assertions.assertNotNull(threadLocal);
    m_value = value;
  }

  @Override
  public IUndecorator decorate() throws Exception {
    final THREAD_LOCAL originValue = m_threadLocal.get();
    m_threadLocal.set(m_value);

    // Restore value upon completion of the command.
    return new IUndecorator() {

      @Override
      public void undecorate(final Throwable throwable) {
        if (originValue == null) {
          m_threadLocal.remove();
        }
        else {
          m_threadLocal.set(originValue);
        }
      }
    };
  }

  public ThreadLocal<THREAD_LOCAL> getThreadLocal() {
    return m_threadLocal;
  }
}
