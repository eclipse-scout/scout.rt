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

import org.eclipse.scout.commons.exception.IProcessingStatus;

/**
 * Marker subclass so we know inside setValue that this was a previous
 * validation failure that was catched. Once validation is successful the error
 * status of this type can safely be cleared.
 */
public class ValidationFailedStatus extends ScoutFieldStatus {
  private static final long serialVersionUID = 1L;

  public ValidationFailedStatus(String message) {
    super(message, WARNING);
  }

  public ValidationFailedStatus(IProcessingStatus s) {
    super(s, WARNING);
  }
}
