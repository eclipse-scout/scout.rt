/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.io.Serial;

/**
 * Thrown if a UI session cannot be created or registered because the HTTP session is invalid or is about to be
 * invalidated.
 */
public class InvalidHttpSessionException extends UiException {
  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidHttpSessionException(String message) {
    super(message);
  }

  public InvalidHttpSessionException(String message, Throwable cause) {
    super(message, cause);
  }
}
