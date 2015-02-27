/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.exception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.commons.Assertions;

/**
 * Core exception in Scout.
 *
 * @see org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService
 */
public class ProcessingException extends Exception implements Serializable {
  private static final long serialVersionUID = 1L;

  private ProcessingStatus m_status;
  private transient boolean m_consumed;

  /**
   * Empty constructor is used to support auto-webservice publishing with java
   * bean support
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
    if (m_status != null) {
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
   * @return <code>true</code> to indicate that a blocking thread was interrupted while waiting for a condition to
   *         become <code>true</code>.
   */
  public boolean isInterruption() {
    return m_status != null && (m_status.getException() instanceof InterruptedException);
  }

  @Override
  public String toString() {
    Assertions.assertNotNull(m_status);
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(super.toString());
    if (m_status.getException() == this) {
      sb.append(" severity=" + m_status.getSeverityName());
      sb.append(" code=" + m_status.getCode());
      if (m_status.getContextMessages().size() > 0) {
        for (String s : m_status.getContextMessages()) {
          sb.append(" ");
          sb.append(s);
          sb.append(" /");
        }
        sb.append(" ");
      }
    }
    else {
      sb.append(m_status.toString());
    }
    if (isConsumed()) {
      sb.append(" consumed");
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * @return the complete stacktrace of the Throwable and all its causes
   *         (recursive)
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
