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

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Internal marker status for parsing errors.
 *
 * @see AbstractValueField#parseValue(String)
 */
@Order(10)
public final class ParsingFailedStatus extends ScoutFieldStatus {
  private static final long serialVersionUID = 1L;
  private final String m_parseInputString;

  public ParsingFailedStatus(String message, String parseInputString) {
    super(message, ERROR);
    m_parseInputString = parseInputString;
  }

  public ParsingFailedStatus(ProcessingException exception, String parseInputString) {
    this(exception.getStatus().getMessage(), parseInputString);
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
