/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Event fired once a job is done, meaning completed, failed or cancelled.
 *
 * @see IFuture#whenDone(IDoneHandler, RunContext)
 * @since 5.1
 */
public class DoneEvent<RESULT> {

  private final RESULT m_result;
  private final Throwable m_exception;
  private final boolean m_cancelled;

  public DoneEvent(final RESULT result, final Throwable exception, final boolean cancelled) {
    m_result = result;
    m_exception = exception;
    m_cancelled = cancelled;
  }

  /**
   * @return result if the job completed successfully.
   */
  public RESULT getResult() {
    return m_result;
  }

  /**
   * @return exception due to abnormal completion.
   * @see #isFailed()
   */
  public Throwable getException() {
    return m_exception;
  }

  /**
   * @return <code>true</code> if the job was cancelled.
   */
  public boolean isCancelled() {
    return m_cancelled;
  }

  /**
   * @return <code>true</code> if the job terminated with an exception.
   */
  public boolean isFailed() {
    return m_exception != null;
  }
}
