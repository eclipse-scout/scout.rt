/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.status.Status;

/**
 * Internal marker status for validation errors during setValue.
 *
 * @see AbstractValueField#validateValueInternal(Object)
 */
@Order(20)
public final class ValidationFailedStatus<VALUE> extends Status {

  private static final long serialVersionUID = 1L;
  private final VALUE m_invalidValue;

  public ValidationFailedStatus(String message) {
    this(message, ERROR);
  }

  public ValidationFailedStatus(String message, int severity) {
    this(message, severity, 0);
  }

  public ValidationFailedStatus(String message, int severity, int code) {
    this(message, severity, code, null);
  }

  public ValidationFailedStatus(String message, int severity, int code, VALUE invalidValue) {
    super(message, severity, code);
    m_invalidValue = invalidValue;
  }

  public ValidationFailedStatus(ProcessingException e, VALUE invalidValue) {
    this(e.getStatus().getMessage(), e.getStatus().getSeverity(), e.getStatus().getCode(), invalidValue);
  }

  public VALUE getInvalidValue() {
    return m_invalidValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_invalidValue == null) ? 0 : m_invalidValue.hashCode());
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
    ValidationFailedStatus other = (ValidationFailedStatus) obj;
    if (m_invalidValue == null) {
      if (other.m_invalidValue != null) {
        return false;
      }
    }
    else if (!m_invalidValue.equals(other.m_invalidValue)) {
      return false;
    }
    return true;
  }
}
