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
package org.eclipse.scout.commons.job;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Thread factory for named threads and to handle uncaught exceptions.
 *
 * @since 5.1
 */
public class NamedThreadFactory implements ThreadFactory {

  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(NamedThreadFactory.class);

  private final String m_name;
  private final ThreadFactory m_delegate;
  private final AtomicLong m_sequence;

  public NamedThreadFactory(final String threadName) {
    m_name = String.format("%s-", threadName);
    m_delegate = Executors.defaultThreadFactory();
    m_sequence = new AtomicLong();
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    final Thread thread = m_delegate.newThread(runnable);
    thread.setName(m_name + String.valueOf(m_sequence.incrementAndGet()));
    thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(final Thread t, final Throwable throwable) {
        handleUncaughtException(t, throwable);
      }
    });
    return thread;
  }

  /**
   * Method invoked when an uncaught exception is thrown.
   */
  protected void handleUncaughtException(final Thread thread, final Throwable throwable) {
    LOG.error(String.format("Worker thread abruptly terminated due to an uncaught exception [thread=%s]", thread.getName()), throwable);
  }
}
