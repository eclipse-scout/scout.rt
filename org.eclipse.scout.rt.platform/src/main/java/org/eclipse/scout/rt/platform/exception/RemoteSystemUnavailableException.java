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
 * Exception thrown in case of any error during communication with a remote part of the system.
 */
public class RemoteSystemUnavailableException extends PlatformException {
  private static final long serialVersionUID = 1L;

  public RemoteSystemUnavailableException(String message, Object... args) {
    super(message, args);
  }
}
