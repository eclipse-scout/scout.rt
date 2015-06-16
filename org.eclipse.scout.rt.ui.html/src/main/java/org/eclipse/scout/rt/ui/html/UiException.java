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
