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
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;

/**
 * Internal marker status for parsing errors.
 *
 * @see AbstractValueField#parseValueInternal(String)
 */
@Order(10)
public final class ParsingFailedStatus extends Status {
  private static final long serialVersionUID = 1L;
  private final String m_parseInputString;

  public ParsingFailedStatus(String message, String parseInputString) {
    super(message, ERROR);
    m_parseInputString = parseInputString;
  }

  public ParsingFailedStatus(IStatus status, String parseInputString) {
    super(status.getMessage(), status.getSeverity(), status.getCode(), status.getIconId());
    m_parseInputString = parseInputString;
  }

  public ParsingFailedStatus(ProcessingException exception, String parseInputString) {
    this(exception.getStatus().getMessage(), parseInputString);
    // it's important to store not only the status message, but also the status code, because
    // some business logic may depend on a certain code, e.g. the smart-field
    setCode(exception.getStatus().getCode());
  }

  public String getParseInputString() {
    return m_parseInputString;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_parseInputString == null) ? 0 : m_parseInputString.hashCode());
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
    ParsingFailedStatus other = (ParsingFailedStatus) obj;
    if (m_parseInputString == null) {
      if (other.m_parseInputString != null) {
        return false;
      }
    }
    else if (!m_parseInputString.equals(other.m_parseInputString)) {
      return false;
    }
    return true;
  }
}
