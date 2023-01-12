/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
