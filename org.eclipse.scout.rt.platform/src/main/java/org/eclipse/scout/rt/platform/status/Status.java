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
package org.eclipse.scout.rt.platform.status;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Status
 */
@Order(IOrdered.DEFAULT_ORDER)
public class Status implements IStatus, Serializable {
  private static final long serialVersionUID = 382223180907716448L;

  /**
   * default ok status.
   */
  public static final Status OK_STATUS = new Status(OK);

  private final int m_severity;
  private final String m_message;
  private final int m_code;

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
   * for Serialization
   */
  public Status() {
    this("undefined");
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
    return StringUtility.emptyIfNull(m_message);
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
    else if (o.getOrder() - getOrder() != 0) {
      return Double.valueOf(getOrder() - o.getOrder()).intValue();
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
    return getClass().getSimpleName() + " [" + "severity=" + getSeverityName() + ", message=" + m_message + "]";
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
    if (getOrder() != other.getOrder()) {
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
    return getSeverity() == OK;
  }

  @Override
  public double getOrder() {
    Class<?> clazz = getClass();
    while (clazz.getAnnotation(Order.class) == null) {
      clazz = clazz.getSuperclass();
    }
    return clazz.getAnnotation(Order.class).value();
  }

}
