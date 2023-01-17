/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

  private int m_severity;
  private final String m_message;
  private int m_code;
  private String m_iconId;
  private final double m_order;

  /**
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(int severity) {
    this(null, severity);
  }

  /**
   * Creates a status with severity {@link #ERROR} and a given message
   */
  public Status(String message) {
    this(message, IStatus.ERROR);
  }

  /**
   * Status with message and severity.
   *
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(String message, int severity) {
    this(message, severity, 0);
  }

  /**
   * Status with message and severity, error code
   *
   * @param severity
   *          {@link #ERROR}, {@link #WARNING}, {@link #INFO}
   */
  public Status(String message, int severity, int code) {
    this(message, severity, code, null);
  }

  /**
   * Status with message, severity and icon.
   */
  public Status(String message, int severity, String iconId) {
    this(message, severity, 0, iconId);
  }

  /**
   * Status with message, severity, code and icon.
   */
  public Status(String message, int severity, int code, String iconId) {
    m_severity = severity;
    m_message = message;
    m_code = code;
    // parse order
    Class<?> clazz = getClass();
    while (clazz.getAnnotation(Order.class) == null) {
      clazz = clazz.getSuperclass();
    }
    m_order = clazz.getAnnotation(Order.class).value();
    m_iconId = iconId;
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

  public void setSeverity(int severity) {
    withSeverity(severity);
  }

  public Status withSeverity(int severity) {
    m_severity = severity;
    return this;
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
  public String getIconId() {
    return m_iconId;
  }

  public Status withIconId(String iconId) {
    m_iconId = iconId;
    return this;
  }

  @Override
  public int getCode() {
    return m_code;
  }

  public void setCode(int code) {
    m_code = code;
  }

  public Status withCode(int code) {
    m_code = code;
    return this;
  }

  @Override
  public boolean isOK() {
    return getSeverity() == OK;
  }

  @Override
  public double getOrder() {
    return m_order;
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
      return (int) ((getOrder() - o.getOrder()));
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
    return Status.getSeverityName(getSeverity());
  }

  public static String getSeverityName(int severity) {
    switch (severity) {
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
    result = prime * result + m_code;
    result = prime * result + ((m_iconId == null) ? 0 : m_iconId.hashCode());
    result = prime * result + ((m_message == null) ? 0 : m_message.hashCode());
    long temp;
    temp = Double.doubleToLongBits(m_order);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (m_code != other.m_code) {
      return false;
    }
    if (m_iconId == null) {
      if (other.m_iconId != null) {
        return false;
      }
    }
    else if (!m_iconId.equals(other.m_iconId)) {
      return false;
    }
    if (m_message == null) {
      if (other.m_message != null) {
        return false;
      }
    }
    else if (!m_message.equals(other.m_message)) {
      return false;
    }
    if (Double.doubleToLongBits(m_order) != Double.doubleToLongBits(other.m_order)) {
      return false;
    }
    if (m_severity != other.m_severity) {
      return false;
    }
    return true;
  }

}
