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

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * A Status for processing results.
 */
public class ProcessingStatus extends Status implements IProcessingStatus, Serializable {
  private static final long serialVersionUID = 1L;

  public static final ProcessingStatus OK_STATUS = new ProcessingStatus(Status.OK_STATUS) {
    private static final long serialVersionUID = 1L;

    /**
     * ensure unmodifiable
     */
    @Override
    public void setBody(String messageBody) {
      throw new UnsupportedOperationException("The default OK STATUS is unmodifiable.");
    }

    /**
     * ensure unmodifiable
     */
    @Override
    public void setException(Throwable exception) {
      throw new UnsupportedOperationException("The default OK STATUS is unmodifiable.");
    }

    /**
     * ensure unmodifiable
     */
    @Override
    public void setTitle(String title) {
      throw new UnsupportedOperationException("The default OK STATUS is unmodifiable.");
    }
  };
  /**
   * Title, localized to the current locale.
   */
  private String m_messageTitle;

  /**
   * Message, localized to the current locale.
   */
  private String m_messageBody = "";

  /**
   * Wrapped exception, or <code>null</code> if none.
   */
  private Throwable m_exception = null;

  /**
   * Creates a new status with severity {@link IStatus#ERROR}.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</code>
   */
  public ProcessingStatus(String messageBody) {
    this(messageBody, IStatus.ERROR);
  }

  /**
   * Creates a new status without child-statuses.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</code>
   * @param severity
   *          the severity; exactly one of {@link #FATAL}, {@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #OK}
   */
  public ProcessingStatus(String messageBody, int severity) {
    this(null, messageBody, severity);
  }

  /**
   * Creates a new status without child-statuses.
   *
   * @param messageTitle
   *          a human-readable title, localized to the current locale, can be <code>null</code>
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</code>
   * @param severity
   *          the severity; exactly one of {@link #FATAL}, {@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #OK}
   */
  public ProcessingStatus(String messageTitle, String messageBody, int severity) {
    this(messageTitle, messageBody, null, 0, severity);
  }

  /**
   * Creates a new status without child-statuses.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</code> &#64;param cause a
   *          low-level exception, or <code>null</code> if not applicable
   * @param code
   *          the custom status code
   * @param severity
   *          the severity; exactly one of {@link #FATAL}, {@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #OK}
   */
  public ProcessingStatus(String messageBody, Throwable cause, int code, int severity) {
    this(null, messageBody, cause, code, severity);
  }

  /**
   * Creates a new status without child-statuses.
   *
   * @param messageTitle
   *          a human-readable title, localized to the current locale, can be <code>null</code> &#64;param messageBody a
   *          human-readable message, localized to the current locale, is never <code>null</code> &#64;param cause a
   *          low-level exception, or <code>null</code> if not applicable
   * @param code
   *          the custom status code
   * @param severity
   *          the severity; exactly one of {@link #FATAL}, {@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #OK}
   */
  public ProcessingStatus(String messageTitle, String messageBody, Throwable cause, int code, int severity) {
    super(null, checkSeverity(severity), code);
    m_messageTitle = messageTitle;
    m_messageBody = messageBody;
    m_exception = cause;
  }

  public ProcessingStatus(IStatus s) {
    super(Assertions.assertNotNull(s).getMessage(), checkSeverity(s.getSeverity()), s.getCode());
    m_messageBody = s.getMessage();
    if (s instanceof IProcessingStatus) {
      m_messageTitle = ((IProcessingStatus) s).getTitle();
      m_exception = ((IProcessingStatus) s).getException();
    }
  }

  @Override
  public boolean isMultiStatus() {
    return false;
  }

  @Override
  public Throwable getException() {
    return m_exception;
  }

  /**
   * Status message with {@link ProcessingStatus#getBody() body} and {@link ProcessingStatus#getTitle() title}.
   */
  @Override
  public String getMessage() {
    return StringUtility.join(": ", getTitle(), getBody());
  }

  @Override
  public String getBody() {
    return m_messageBody;
  }

  @Override
  public String getTitle() {
    return m_messageTitle;
  }

  @Override
  public boolean matches(int severityMask) {
    return (getSeverity() & severityMask) != 0;
  }

  /**
   * Sets the exception.
   *
   * @param exception
   *          a low-level exception, or <code>null</code> if not applicable
   */
  public void setException(Throwable exception) {
    m_exception = exception;
  }

  /**
   * Sets the message.
   *
   * @param title
   *          a human-readable message, localized to the current locale
   */
  public void setTitle(String title) {
    m_messageTitle = title;
  }

  /**
   * Sets the message.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale
   */
  public void setBody(String messageBody) {
    m_messageBody = messageBody;
  }

  /**
   * @param severity
   *          the severity; one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
   *          <code>CANCEL</code>
   */
  private static int checkSeverity(int severity) {
    switch (severity) {
      case INFO:
      case WARNING:
      case ERROR:
      case FATAL:
      case OK: {
        return severity;
      }
      default: {
        throw new IllegalArgumentException("illegal severity: " + severity);
      }
    }
  }

  /**
   * Returns a string representation of the status, suitable for debugging purposes only.
   */
  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("title", getTitle(), false);
    builder.attr("body", getBody(), false);
    builder.attr("severity", getSeverityName());
    builder.attr("code", getCode());
    builder.attr("exception", m_exception, false);
    return builder.toString();
  }

  /**
   * severity name ($NON-NLS-1$)
   */
  @Override
  protected String getSeverityName() {
    switch (getSeverity()) {
      case FATAL: {
        return "FATAL";
      }
      default:
        return super.getSeverityName();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_exception == null) ? 0 : m_exception.hashCode());
    result = prime * result + ((m_messageBody == null) ? 0 : m_messageBody.hashCode());
    result = prime * result + ((m_messageTitle == null) ? 0 : m_messageTitle.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ProcessingStatus other = (ProcessingStatus) obj;
    if (m_exception == null) {
      if (other.m_exception != null) {
        return false;
      }
    }
    else if (!m_exception.equals(other.m_exception)) {
      return false;
    }
    if (m_messageBody == null) {
      if (other.m_messageBody != null) {
        return false;
      }
    }
    else if (!m_messageBody.equals(other.m_messageBody)) {
      return false;
    }
    if (m_messageTitle == null) {
      if (other.m_messageTitle != null) {
        return false;
      }
    }
    else if (!m_messageTitle.equals(other.m_messageTitle)) {
      return false;
    }
    return true;
  }
}
