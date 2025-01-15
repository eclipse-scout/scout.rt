/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.exception;

/**
 * This exception is used for initialization errors.
 */
public class InitializationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public InitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * @param message
   * @param cause
   */
  public InitializationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InitializationException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public InitializationException(Throwable cause) {
    super(cause);
  }
}
