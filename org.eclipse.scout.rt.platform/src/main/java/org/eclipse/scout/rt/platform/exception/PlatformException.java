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

import java.io.Serializable;

/**
 * @since 5.2
 */
public class PlatformException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = 1L;

  public PlatformException(String message) {
    super(message);
  }

  public PlatformException(String message, Throwable cause) {
    super(message, cause);
  }

}
