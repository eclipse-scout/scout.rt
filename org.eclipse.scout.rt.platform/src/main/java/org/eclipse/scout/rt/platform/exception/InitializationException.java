/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
