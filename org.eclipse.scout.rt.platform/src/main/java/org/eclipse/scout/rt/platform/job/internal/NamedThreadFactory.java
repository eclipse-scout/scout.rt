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
package org.eclipse.scout.rt.platform.job.internal;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.job.JobExceptionHandler;

/**
 * Thread factory for named threads and to handle uncaught exceptions.
 *
 * @since 5.1
 */
public class NamedThreadFactory implements ThreadFactory {

  private static final Pattern THREAD_SEQUENCE_PATTERN = Pattern.compile("^.+\\-(\\d+?) \\(idle\\)$");

  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(NamedThreadFactory.class);

  private final ThreadFactory m_delegate;
  private final AtomicLong m_sequence;
  private final String m_threadName;

  public NamedThreadFactory(final String threadName) {
    m_threadName = threadName;
    m_delegate = Executors.defaultThreadFactory();
    m_sequence = new AtomicLong();
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    final Thread thread = m_delegate.newThread(runnable);
    thread.setName(m_threadName + "-" + m_sequence.incrementAndGet() + " (idle)");
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(final Thread t, final Throwable cause) {
        try {
          OBJ.get(JobExceptionHandler.class).handleUncaughtException(thread, cause);
        }
        catch (final RuntimeException e) {
          LOG.error(String.format("Failed to handle uncaught exception [thread=%s, cause=%s]", t.getName(), cause), e);
        }
      }
    });
    return thread;
  }

  /**
   * Decorates the current thread's name with the given thread- and job-name.
   */
  public static void decorateThreadName(final String threadName, final String jobName) {
    String suffix = "";
    if (StringUtility.hasText(jobName)) {
      suffix = ";" + jobName;
    }

    final Matcher matcher = THREAD_SEQUENCE_PATTERN.matcher(Thread.currentThread().getName());
    if (matcher.find()) {
      Thread.currentThread().setName(threadName + "-" + matcher.group(1) + suffix);
    }
    else {
      Thread.currentThread().setName(threadName + suffix);
    }
  }
}
