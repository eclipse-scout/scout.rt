/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.status;

import java.io.Serializable;
import java.util.Comparator;

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
  private String m_cssClass;

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
    // parse order
    Class<?> clazz = getClass();
    while (clazz.getAnnotation(Order.class) == null) {
      clazz = clazz.getSuperclass();
    }
    m_order = clazz.getAnnotation(Order.class).value();

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

  // TODO [7.0] aho: deprecate use fluent api
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

  // TODO [7.0] aho: deprecate use fluent api
  public void setCode(int code) {
    m_code = code;
  }

  public Status withCode(int code) {
    m_code = code;
    return this;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  public Status withCssClass(String cssClass) {
    m_cssClass = cssClass;
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
    return Comparator
        .comparing(IStatus::getSeverity, Comparator.reverseOrder())
        .thenComparing(IStatus::getOrder)
        .thenComparing(IStatus::getMessage,Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(IStatus::getIconId,Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(IStatus::getCode)
        .thenComparing(IStatus::getCssClass,Comparator.nullsLast(Comparator.naturalOrder()))
        .compare(this, o);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + "severity=" + getSeverityName() + ", message=" + m_message + ", cssClass=" + m_cssClass + "]";
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
    result = prime * result + ((m_cssClass == null) ? 0 : m_cssClass.hashCode());
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
    if (m_cssClass == null) {
      if (other.m_cssClass != null) {
        return false;
      }
    }
    else if (!m_cssClass.equals(other.m_cssClass)) {
      return false;
    }
    return true;
  }

}
