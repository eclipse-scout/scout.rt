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

import org.eclipse.core.runtime.IStatus;

/**
 * This class is the core exception in scout do not change interface contract
 * since this class is serializable and used in different build versions This
 * class is a conceptual copy of {@link org.eclipse.core.runtime.Status} that
 * also can run in J2EE.
 */
public class ProcessingException extends Exception implements Serializable {
  private static final long serialVersionUID = 1L;

  private IProcessingStatus m_status;
  private transient boolean m_consumed;

  /**
   * Empty constructor is used to support auto-webservice publishing with java
   * bean support
   */
  public ProcessingException() {
    super("undefined", null);
  }

  public ProcessingException(String message) {
    this(new ProcessingStatus(null, message, null, 0, IProcessingStatus.ERROR));
  }

  public ProcessingException(String message, Throwable cause) {
    this(new ProcessingStatus(null, message, cause, 0, IProcessingStatus.ERROR));
  }

  public ProcessingException(String message, Throwable cause, int errorCode) {
    this(new ProcessingStatus(null, message, cause, errorCode, IProcessingStatus.ERROR));
  }

  public ProcessingException(String message, Throwable cause, int errorCode, int severity) {
    this(new ProcessingStatus(null, message, cause, errorCode, severity));
  }

  public ProcessingException(String title, String message) {
    this(new ProcessingStatus(title, message, null, 0, IProcessingStatus.ERROR));
  }

  public ProcessingException(String title, String message, Throwable cause) {
    this(new ProcessingStatus(title, message, cause, 0, IProcessingStatus.ERROR));
  }

  public ProcessingException(String title, String message, Throwable cause, int errorCode) {
    this(new ProcessingStatus(title, message, cause, errorCode, IProcessingStatus.ERROR));
  }

  public ProcessingException(String title, String message, Throwable cause, int errorCode, int severity) {
    this(new ProcessingStatus(title, message, cause, errorCode, severity));
  }

  public ProcessingException(IStatus status) {
    super(status.getMessage(), status.getException());
    m_status = status instanceof ProcessingStatus ? (ProcessingStatus) status : new ProcessingStatus(status);
  }

  public IProcessingStatus getStatus() {
    return m_status;
  }

  public void setStatus(IProcessingStatus status) {
    m_status = status;
  }

  public void addContextMessage(String s) {
    m_status.addContextMessage(s);
  }

  public boolean isConsumed() {
    return m_consumed;
  }

  public void consume() {
    m_consumed = true;
  }

  public boolean isInterruption() {
    return m_status != null && (m_status.getCause() instanceof InterruptedException);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_status.toString() + "]";
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
