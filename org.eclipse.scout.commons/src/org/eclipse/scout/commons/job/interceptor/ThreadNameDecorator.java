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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Assertions;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of execution with the job name.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <R>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ThreadNameDecorator<R> implements Callable<R>, Chainable {

  protected static final Pattern PATTERN_THREAD_NAME = Pattern.compile("thread\\:(.+?)\\;job\\:.+");

  protected final Callable<R> m_next;
  protected final String m_jobName;

  /**
   * Creates a processor to decorate the thread-name of the worker-thread with the job name.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param jobName
   *          job name to decorate the thread name with; must not be <code>null</code>.
   */
  public ThreadNameDecorator(final Callable<R> next, final String jobName) {
    m_next = Assertions.assertNotNull(next);
    m_jobName = Assertions.assertNotNullOrEmpty(jobName);
  }

  @Override
  public R call() throws Exception {
    final Thread thread = Thread.currentThread();

    final String oldThreadName = thread.getName();
    final String newThreadName = String.format("thread:%s;job:%s", getOriginalThreadName(oldThreadName), m_jobName);

    thread.setName(newThreadName);
    try {
      return m_next.call();
    }
    finally {
      thread.setName(oldThreadName);
    }
  }

  /**
   * @return the original thread name as defined by the worker-thread.
   */
  protected String getOriginalThreadName(final String threadName) {
    final Matcher matcher = PATTERN_THREAD_NAME.matcher(threadName);
    if (matcher.find()) {
      return matcher.group(1);
    }
    else {
      return threadName;
    }
  }

  @Override
  public Callable<R> getNext() {
    return m_next;
  }
}
