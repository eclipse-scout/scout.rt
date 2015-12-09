/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util.concurrent;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Indicates that the maximal wait time elapsed while waiting for some condition to become <code>true</code>, e.g. while
 * waiting a job to complete.
 *
 * @since 5.2
 */
public class TimeoutException extends PlatformException {

  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link PlatformException}
   */
  public TimeoutException(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public TimeoutException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
