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

import org.eclipse.scout.rt.platform.status.IStatus;

public interface IProcessingStatus extends IStatus {

  /**
   * a fatal error
   */
  int FATAL = 0x10000000;

  /**
   * A title that may be used as message header for the status. {@link #getMessage()} is composed of {@link #getTitle()}
   * and {@link #getBody()}.
   */
  String getTitle();

  /**
   * The body of the message. {@link #getMessage()} is composed of {@link #getTitle()} and {@link #getBody()}.
   */
  String getBody();

  Throwable getException();
}
