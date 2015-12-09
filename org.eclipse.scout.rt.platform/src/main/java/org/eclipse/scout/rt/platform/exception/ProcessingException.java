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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Represents processing errors that occur during application execution.
 * <p>
 * TODO abr, dwi [15.2] Remove the many constructors and use fluent api instead
 */
public class ProcessingException extends PlatformException {
  private static final long serialVersionUID = 1L;

  private ProcessingStatus m_status;
  private transient boolean m_consumed;

  /**
   * Empty constructor is used to support auto-webservice publishing with java bean support
   */
  public ProcessingException() {
    this("undefined");
  }

  /**
   * See constructor of {@link PlatformException}
   */
  public ProcessingException(final String message, final Object... args) {
    super(message, args);
    initStatus(new ProcessingStatus(null, getMessage(), getCause(), 0, IProcessingStatus.ERROR));
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
    initStatus(status);
  }

  private final void initStatus(IProcessingStatus status) {
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

  @Override
  public ProcessingException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }

  public boolean isConsumed() {
    return m_consumed;
  }

  public void consume() {
    m_consumed = true;
  }

  /**
   * Returns the bare message without severity, code or context messages. This method should be used to show the
   * exception to the user.
   *
   * @since 5.2
   */
  public String getDisplayMessage() {
    return super.getMessage();
  }

  @Override
  public String getMessage() {
    // custom getMessage method to get the same results from pe.toString(), pe.printStackTrace() and using a logger
    final List<String> infos = new ArrayList<>();

    // status
    if (m_status != null) {
      infos.add(String.format("severity=%s", m_status.getSeverityName()));
      if (m_status.getCode() != 0) {
        infos.add(String.format("code=%s", m_status.getCode()));
      }
    }

    // context-infos
    final String contextInfos = StringUtility.join(", ", getContextInfos());
    if (StringUtility.hasText(contextInfos)) {
      infos.add(String.format("context={%s}", contextInfos));
    }

    // message
    final String msg = super.getMessage();
    return String.format("%s [%s]", StringUtility.hasText(msg) ? msg : "<empty>", StringUtility.join(", ", infos));
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
