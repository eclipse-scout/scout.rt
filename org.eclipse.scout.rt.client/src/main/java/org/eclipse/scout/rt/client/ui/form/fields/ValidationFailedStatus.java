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
@Order(20.0)
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

}
