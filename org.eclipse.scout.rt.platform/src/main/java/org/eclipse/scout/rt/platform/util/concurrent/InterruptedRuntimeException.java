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
 * Indicates that a thread was interrupted while waiting for some condition to become <code>true</code>, e.g. while
 * waiting for a job to complete.
 * <p>
 * Unlike {@link java.lang.InterruptedException}, the thread's interrupted status is not cleared when catching this
 * exception.
 *
 * @since 5.2
 */
public class InterruptedRuntimeException extends PlatformException {

  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link PlatformException}
   */
  public InterruptedRuntimeException(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public InterruptedRuntimeException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
