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
package org.eclipse.scout.commons.status;

/**
 * Status
 */
public class Status implements IStatus {

  private final int m_severity;
  private final String m_message;
  private final int m_code;

  /**
   * Status with error message and severity, error code
   *
   * @param message
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(String message, int severity, int code) {
    m_severity = severity;
    m_message = message;
    m_code = code;
  }

  /**
   * Status with error message and severity.
   *
   * @param message
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(String message, int severity) {
    this(message, severity, 0);
  }

  /**
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(int severity) {
    this(null, severity);
  }

  /**
   * Creates a status with severity {@link #ERROR} and a given message
   *
   * @param message
   */
  public Status(String message) {
    this(message, IStatus.ERROR);
  }

  @Override
  public int getSeverity() {
    return m_severity;
  }

  @Override
  public boolean matches(int severityMask) {
    return (m_severity & severityMask) != 0;
  }

  @Override
  public String getMessage() {
    return m_message;
  }

  @Override
  public boolean isMultiStatus() {
    return false;
  }

  @Override
  public int compareTo(IStatus o) {
    if (o.getSeverity() - getSeverity() != 0) {
      return o.getSeverity() - getSeverity();
    }
    else if (getMessage() != null && o.getMessage() != null) {
      return getMessage().compareTo(o.getMessage());
    }
    else if (getMessage() != null) {
      return -1;
    }
    return 0;
  }

  @Override
  public String toString() {
    final String severityName = getSeverityName();
    final String severity = severityName != null ? severityName : "severity=" + getSeverity();
    return "Status [" + severity + ", m_message=" + m_message + "]";
  }

  /**
   * severity name ($NON-NLS-1$)
   */
  protected String getSeverityName() {
    switch (getSeverity()) {
      case OK: {
        return "OK";
      }
      case INFO: {
        return "INFO";
      }
      case WARNING: {
        return "WARNING";
      }
      case ERROR: {
        return "ERROR";
      }
      default:
        return "undefined";
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_message == null) ? 0 : m_message.hashCode());
    result = prime * result + m_severity;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Status other = (Status) obj;
    if (m_message == null) {
      if (other.m_message != null) {
        return false;
      }
    }
    else if (!m_message.equals(other.m_message)) {
      return false;
    }
    if (m_severity != other.m_severity) {
      return false;
    }
    return true;
  }

  @Override
  public int getCode() {
    return m_code;
  }

  @Override
  public boolean isOK() {
    return m_severity == OK;
  }

}
