/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
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
 * Exception thrown in case of any error during communication with a remote part of the system.
 */
public class RemoteSystemUnavailableException extends PlatformException {
  private static final long serialVersionUID = 1L;

  public RemoteSystemUnavailableException(String message, Object... args) {
    super(message, args);
  }
}
