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
import java.util.List;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.commons.status.Status;

/**
 * A Status for processing results.
 */
public class ProcessingStatus extends Status implements IProcessingStatus, Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Title, localized to the current locale.
   */
  private String m_messageTitle;

  /**
   * Message, localized to the current locale.
   */
  private String m_messageBody = "";

  private List<String> m_contextMessages;

  /**
   * Wrapped exception, or <code>null</code> if none.
   */
  private Throwable m_exception = null;

  /**
   * Creates a new status with severity {@link IStatus#ERROR}.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</null>
   */
  public ProcessingStatus(String messageBody) {
    this(messageBody, IStatus.ERROR);
  }

  /**
   * Creates a new status without child-statuses.
   *
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</null>
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
   *          a human-readable title, localized to the current locale, can be <code>null</null>
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</null>
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
   *          a human-readable message, localized to the current locale, is never <code>null</null>
   * @param cause
   *          a low-level exception, or <code>null</code> if not applicable
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
   *          a human-readable title, localized to the current locale, can be <code>null</null>
   * @param messageBody
   *          a human-readable message, localized to the current locale, is never <code>null</null>
   * @param cause
   *          a low-level exception, or <code>null</code> if not applicable
   * @param code
   *          the custom status code
   * @param severity
   *          the severity; exactly one of {@link #FATAL}, {@link #ERROR}, {@link #WARNING}, {@link #INFO}, {@link #OK}
   */
  public ProcessingStatus(String messageTitle, String messageBody, Throwable cause, int code, int severity) {
    super(null, checkSeverity(severity), code);
    setTitle(messageTitle);
    setBody(messageBody);
    setException(cause);
  }

  public ProcessingStatus(IStatus s) {
    super(Assertions.assertNotNull(s).getMessage(), checkSeverity(s.getSeverity()), s.getCode());
    setBody(s.getMessage());
    if (s instanceof IProcessingStatus) {
      setTitle(((IProcessingStatus) s).getTitle());
      setException(((IProcessingStatus) s).getException());
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
   * Status message with {@link #getBody() body} and {@link #getTitle() title}.
   */
  @Override
  public String getMessage() {
    StringBuilder buf = new StringBuilder();
    if (getTitle() != null) {
      buf.append(getTitle());
    }
    if (getBody() != null) {
      if (buf.length() > 0) {
        buf.append("\n");
      }
      buf.append(getBody());
    }
    return buf.toString();
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

  @Override
  public void addContextMessage(String message) {
    if (message != null) {
      if (m_contextMessages == null) {
        m_contextMessages = new ArrayList<String>();
      }
      m_contextMessages.add(0, message);
    }
  }

  @Override
  public List<String> getContextMessages() {
    return CollectionUtility.arrayList(m_contextMessages);
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
   * Returns a string representation of the status, suitable for debugging
   * purposes only.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + "["); //$NON-NLS-1$
    sb.append(" severity=" + getSeverityName());
    sb.append(" code=" + getCode()); //$NON-NLS-1$
    if (m_contextMessages != null) {
      for (String s : m_contextMessages) {
        sb.append(" ");
        sb.append(s);
        sb.append(" /");
      }
    }
    sb.append(" ");
    sb.append(getTitle());
    sb.append(" ");
    sb.append(getBody());
    if (m_exception != null) {
      sb.append(" ");
      sb.append(m_exception.toString());
    }
    sb.append("]"); //$NON-NLS-1$
    return sb.toString();
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
    result = prime * result + ((m_contextMessages == null) ? 0 : m_contextMessages.hashCode());
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
    if (m_contextMessages == null) {
      if (other.m_contextMessages != null) {
        return false;
      }
    }
    else if (!m_contextMessages.equals(other.m_contextMessages)) {
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
