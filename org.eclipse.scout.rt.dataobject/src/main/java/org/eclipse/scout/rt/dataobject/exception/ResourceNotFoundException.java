/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.exception;

import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Indicates that a resource requested by a client could not be found.
 * <p>
 * May be used for HTTP 404 - "Not Found"
 */
public class ResourceNotFoundException extends VetoException {
  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException() {
    super();
  }

  public ResourceNotFoundException(String message, Object... args) {
    super(message, args);
  }

  public ResourceNotFoundException(IProcessingStatus status) {
    super(status);
  }
}
