/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Indicates that the result of a job cannot be retrieved, or the {@link IFuture}'s completion not be awaited because
 * the job was cancelled.
 *
 * @since 6.1
 */
public class FutureCancelledError extends AbstractInterruptionError {
  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link AbstractInterruptionError}
   */
  public FutureCancelledError(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public FutureCancelledError withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
