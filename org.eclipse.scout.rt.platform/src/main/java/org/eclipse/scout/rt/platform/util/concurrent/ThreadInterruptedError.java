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

/**
 * Indicates that a thread was interrupted while waiting for some condition to become <code>true</code>, e.g. while
 * waiting for a job to complete.
 * <p>
 * Unlike {@link InterruptedException}, the thread's interrupted status is not cleared when catching this error.
 *
 * @since 5.2
 */
public class ThreadInterruptedError extends AbstractInterruptionError {

  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link AbstractInterruptionError}
   */
  public ThreadInterruptedError(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public ThreadInterruptedError withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
