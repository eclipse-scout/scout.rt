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
package org.eclipse.scout.commons.job.internal.callable;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IJobInput;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of execution with the job name.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ThreadNameDecorator<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

  @Internal
  protected static final Pattern PATTERN_THREAD_NAME = Pattern.compile("(.+?)\\;job\\:.+");
  @Internal
  protected final Callable<RESULT> m_next;
  @Internal
  protected final String m_identifier;

  /**
   * Creates a processor to decorate the thread-name of the worker-thread with the job name.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param input
   *          input to decorate the thread name with the job's <code>id</code> and name.
   */
  public ThreadNameDecorator(final Callable<RESULT> next, final IJobInput input) {
    m_next = Assertions.assertNotNull(next);
    m_identifier = input.getIdentifier();
  }

  @Override
  public RESULT call() throws Exception {
    final Thread thread = Thread.currentThread();

    final String oldThreadName = thread.getName();
    final String originalThreadName = getOriginalThreadName(oldThreadName);

    final String newThreadName;
    if (IJobInput.IDENTIFIER_UNKNOWN.equals(m_identifier)) {
      newThreadName = originalThreadName;
    }
    else {
      newThreadName = String.format("%s;job:%s", originalThreadName, m_identifier);
    }

    thread.setName(newThreadName);
    try {
      return m_next.call();
    }
    finally {
      thread.setName(oldThreadName);
    }
  }

  /**
   * Removes a potential job decoration contributed by a dependent job.
   *
   * @return the original thread name as defined by the worker-thread.
   */
  @Internal
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
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
