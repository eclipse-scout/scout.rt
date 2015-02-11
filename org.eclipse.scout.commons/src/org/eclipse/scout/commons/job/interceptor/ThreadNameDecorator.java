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
import org.eclipse.scout.commons.job.IJob;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of executing an {@link IJob}.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <R>
 *          the result type of the job's computation.
 * @since 5.0
 */
public class ThreadNameDecorator<R> implements Callable<R> {

  protected static final Pattern PATTERN_THREAD_NAME = Pattern.compile("thread\\:(.+?)\\;job\\:.+");

  protected final Callable<R> m_next;
  protected final String m_jobName;

  /**
   * @param next
   *          the next processor in the chain.
   * @param jobName
   *          the name of the job to decorate the thread's name.
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
}
