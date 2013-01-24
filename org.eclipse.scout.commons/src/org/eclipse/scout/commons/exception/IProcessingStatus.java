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

import org.eclipse.core.runtime.IStatus;

public interface IProcessingStatus extends IStatus {

  int FATAL = 0x10;

  /**
   * alias for {@link #getException()}
   */
  Throwable getCause();

  String getTitle();

  /**
   * Returns a list of context informations that were collected while the
   * exception was travelling from its origin to the handler
   * 
   * @return a list of localized contect messages
   */
  String[] getContextMessages();

  void addContextMessage(String message);
}
