/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

/**
 * A runtime exception used by the Html UI.
 */
public class UiException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UiException(String message) {
    super(message);
  }

  public UiException(Throwable cause) {
    super(cause);
  }

  public UiException(String message, Throwable cause) {
    super(message, cause);
  }

  public UiException(String message, Throwable cause, Object... messageArguments) {
    super(String.format(message, messageArguments), cause);
  }
}
