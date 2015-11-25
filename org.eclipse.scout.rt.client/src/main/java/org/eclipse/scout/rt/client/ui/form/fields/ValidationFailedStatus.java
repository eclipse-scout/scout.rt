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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Internal marker status for validation errors during setValue.
 *
 * @see AbstractValueField#validateValueInternal(Object)
 * @param invalid
 *          value type
 */
@Order(20)
public final class ValidationFailedStatus<VALUE> extends ScoutFieldStatus {

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
