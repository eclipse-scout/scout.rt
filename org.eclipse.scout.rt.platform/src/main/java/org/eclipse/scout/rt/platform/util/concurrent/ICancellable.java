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
 * This is any kind of object interested in active cancellation.
 *
 * @since 5.1
 */
public interface ICancellable {

  /**
   * Attempts to cancel the execution of this {@link ICancellable}.
   *
   * @param interruptIfRunning
   *     <code>true</code> if the thread executing this {@link ICancellable} should be interrupted.
   * @return <code>false</code> if cancellation failed, typically because it has already cancelled or completed.
   */
  boolean cancel(boolean interruptIfRunning);

  /**
   * @return <code>true</code> if this {@link ICancellable} was cancelled.
   */
  boolean isCancelled();
}
