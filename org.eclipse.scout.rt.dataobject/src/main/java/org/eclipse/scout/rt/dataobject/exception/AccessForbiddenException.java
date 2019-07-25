/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.exception;

import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Indicates that a resource requested by a client can not be accessed.
 * <p>
 * May be used for HTTP 403 - "Forbidden"
 */
public class AccessForbiddenException extends VetoException {
  private static final long serialVersionUID = 1L;

  public AccessForbiddenException() {
    super();
  }

  public AccessForbiddenException(String message, Object... args) {
    super(message, args);
  }

  public AccessForbiddenException(IProcessingStatus status) {
    super(status);
  }
}
