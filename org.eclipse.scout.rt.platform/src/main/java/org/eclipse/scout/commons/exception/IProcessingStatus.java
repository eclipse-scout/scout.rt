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
package org.eclipse.scout.commons.exception;

import java.util.List;

import org.eclipse.scout.commons.status.IStatus;

public interface IProcessingStatus extends IStatus {

  /** a fatal error */
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

  /**
   * Returns a list of context informations that were collected while the exception was traveling from its origin to the
   * handler
   *
   * @return a list of localized context messages
   */
  List<String> getContextMessages();

  void addContextMessage(String message);

  Throwable getException();

}
