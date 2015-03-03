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
 * Internal marker status for parsing errors.
 *
 * @see AbstractValueField#parseValue(String)
 */
@Order(10.0)
public final class ParsingFailedStatus extends ScoutFieldStatus {
  private static final long serialVersionUID = 1L;
  private final String m_parseInputString;

  public ParsingFailedStatus(String message, String parseInputString) {
    super(message, ERROR);
    m_parseInputString = parseInputString;
  }

  public ParsingFailedStatus(ProcessingException exception, String parseInputString) {
    this(exception.getMessage(), parseInputString);
  }

  public String getParseInputString() {
    return m_parseInputString;
  }
}
