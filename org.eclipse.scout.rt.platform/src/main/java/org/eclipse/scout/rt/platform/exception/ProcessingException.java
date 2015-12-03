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
package org.eclipse.scout.rt.platform.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Core exception in Scout.
 *
 * @see org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService
 */
public class ProcessingException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 1L;

  private ProcessingStatus m_status;
  private transient boolean m_consumed;

  /**
   * Empty constructor is used to support auto-webservice publishing with java bean support
   */
  public ProcessingException() {
    this("undefined");
  }

  public ProcessingException(String message) {
    this(null, message);
  }

  public ProcessingException(String message, Throwable cause) {
    this(null, message, cause);
  }

  public ProcessingException(String message, Throwable cause, int errorCode) {
    this(null, message, cause, errorCode);
  }

  public ProcessingException(String message, Throwable cause, int errorCode, int severity) {
    this(null, message, cause, errorCode, severity);
  }

  public ProcessingException(String title, String message) {
    this(title, message, null);
  }

  public ProcessingException(String title, String message, Throwable cause) {
    this(title, message, cause, 0);
  }

  public ProcessingException(String title, String message, Throwable cause, int errorCode) {
    this(title, message, cause, errorCode, IProcessingStatus.ERROR);
  }

  public ProcessingException(String title, String message, Throwable cause, int errorCode, int severity) {
    this(new ProcessingStatus(title, message, cause, errorCode, severity));
  }

  public ProcessingException(IProcessingStatus status) {
    super(status.getMessage(), status.getException());
    m_status = status instanceof ProcessingStatus ? (ProcessingStatus) status : new ProcessingStatus(status);
    if (m_status.getException() == null) {
      ((ProcessingStatus) status).setException(this);
    }
  }

  public IProcessingStatus getStatus() {
    return m_status;
  }

  public void setStatus(IProcessingStatus status) {
    m_status = new ProcessingStatus(status);
  }

  /**
   * Adds the given message to the list of context messages at first position.
   *
   * @param msg
   *          context message to be added.
   */
  public void addContextMessage(String msg) {
    if (m_status != null) {
      m_status.addContextMessage(msg);
    }
  }

  /**
   * Adds the given message to the list of context messages at first position.
   *
   * @param msg
   *          context message to be added.
   * @param msgArgs
   *          arguments to be used in the message; see {@link String#format(String, Object...)}.
   */
  public void addContextMessage(String msg, Object... msgArgs) {
    if (m_status != null && msg != null) {
      m_status.addContextMessage(String.format(msg, msgArgs));
    }
  }

  public boolean isConsumed() {
    return m_consumed;
  }

  public void consume() {
    m_consumed = true;
  }

  /**
   * @return <code>true</code> to indicate that a thread waiting for a lock or condition was interrupted.
   */
  public boolean isInterruption() {
    return m_status != null && (m_status.getException() instanceof InterruptedException);
  }

  /**
   * @return <code>true</code> to indicate that processing was cancelled.
   */
  public boolean isCancellation() {
    return m_status != null && (m_status.getException() instanceof CancellationException);
  }

  /**
   * @return <code>true</code> to indicate that a timeout elapsed while waiting for an operation to complete, e.g. a
   *         job's execution.
   */
  public boolean isTimeout() {
    return m_status != null && (m_status.getException() instanceof TimeoutException);
  }

  @Override
  public String getMessage() {
    // custom getMessage method to get the same results from pe.toString(), pe.printStackTrace() and using a logger
    String msg = super.getMessage();
    if (m_status != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(StringUtility.hasText(msg) ? msg : "<empty>");
      sb.append(" [severity=").append(m_status.getSeverityName());
      if (m_status.getCode() != 0) {
        sb.append(", code=").append(m_status.getCode());
      }
      String ctx = StringUtility.join(", ", m_status.getContextMessages());
      if (StringUtility.hasText(ctx)) {
        sb.append(", context={").append(ctx).append("}");
      }
      sb.append("]");
      return sb.toString();
    }
    return msg;
  }

  /**
   * @return the complete stacktrace of the Throwable and all its causes (recursive)
   */
  public static StackTraceElement[] unionStackTrace(Throwable t) {
    ArrayList<StackTraceElement> list = new ArrayList<StackTraceElement>();
    while (t != null) {
      list.addAll(0, Arrays.asList(t.getStackTrace()));
      t = t.getCause();
    }
    return list.toArray(new StackTraceElement[list.size()]);
  }
}
