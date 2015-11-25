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
package org.eclipse.scout.rt.platform.job;

/**
 * Event fired once a job is done, meaning completed, failed or cancelled.
 *
 * @see IFuture#whenDone(IDoneCallback)
 * @since 5.1
 */
public class DoneEvent<RESULT> {

  private final RESULT m_result;
  private final Throwable m_error;
  private final boolean m_cancelled;

  public DoneEvent(final RESULT result, final Throwable error, final boolean cancelled) {
    m_result = result;
    m_error = error;
    m_cancelled = cancelled;
  }

  /**
   * @return result if the job completed successfully.
   */
  public RESULT getResult() {
    return m_result;
  }

  /**
   * @return error due to abnormal completion.
   * @see #isFailed()
   */
  public Throwable getError() {
    return m_error;
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
    return m_error != null;
  }
}
