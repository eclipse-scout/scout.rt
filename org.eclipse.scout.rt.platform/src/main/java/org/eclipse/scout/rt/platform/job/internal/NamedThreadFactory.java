/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Thread factory for named threads and to handle uncaught exceptions.
 *
 * @since 5.1
 */
public class NamedThreadFactory implements ThreadFactory, UncaughtExceptionHandler {

  protected static final Logger LOG = LoggerFactory.getLogger(NamedThreadFactory.class);

  private final AtomicLong m_sequence;
  private final String m_threadName;
  private final ThreadGroup m_group;

  public NamedThreadFactory(final String threadName) {
    m_threadName = threadName;
    m_sequence = new AtomicLong();

    final SecurityManager securityManager = System.getSecurityManager();
    m_group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    final AtomicReference<ThreadInfo> threadInfoRef = new AtomicReference<>();
    final Thread thread = new Thread(m_group, runnable, m_threadName, 0) {

      @Override
      public void run() {
        /*
         * Workaround to clear inherited Mapped Diagnostic Context (MDC) which would otherwise preserve
         * and log MDC entries which were valid when the new worker thread was created.
         * Logback < 1.1.5 is affected (see https://jira.qos.ch/browse/LOGBACK-422)
         */
        MDC.clear();

        ThreadInfo.CURRENT.set(threadInfoRef.get());
        try {
          super.run();
        }
        finally {
          ThreadInfo.CURRENT.remove();
        }
      }
    };
    threadInfoRef.set(new ThreadInfo(thread, m_threadName, m_sequence.incrementAndGet()));

    thread.setDaemon(false);
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.setUncaughtExceptionHandler(this);

    return thread;
  }

  // === UncaughtExceptionHandler ===

  @Override
  public void uncaughtException(final Thread thread, final Throwable t) {
    try {
      // Worker thread abruptly terminated due to an uncaught exception.
      BEANS.get(ExceptionHandler.class).handle(t);
    }
    catch (final Throwable unhandledThrowable) { // NOSONAR
      LOG.error("Unexpected: Unhandled throwable during job execution", unhandledThrowable);
    }
  }

  /**
   * Information about the worker thread.
   */
  public static class ThreadInfo {

    /**
     * The {@link ThreadInfo} which is currently associated with the current thread.
     */
    public static final ThreadLocal<ThreadInfo> CURRENT = new ThreadLocal<>();

    private final Thread m_thread;

    private final String m_originalThreadName;
    private final long m_sequence;

    public ThreadInfo(final Thread thread, final String threadName, final long sequence) {
      m_thread = thread;
      m_originalThreadName = threadName;
      m_sequence = sequence;

      reset();
    }

    /**
     * Resets the worker thread name to its original name.
     */
    public void reset() {
      updateThreadName(m_originalThreadName, null);
    }

    /**
     * Updates the thread name with the given name and some optional execution information.
     *
     * @param threadName
     *          the name of the thread
     * @param executionInfo
     *          optional info to be appended to the thread name, else <code>null</code>.
     */
    public void updateThreadName(final String threadName, final String executionInfo) {
      String name = String.format("%s-%s", ObjectUtility.nvl(threadName, m_originalThreadName), m_sequence);
      if (StringUtility.hasText(executionInfo)) {
        name = String.format("%s %s", name, executionInfo);
      }

      m_thread.setName(name);
    }
  }
}
