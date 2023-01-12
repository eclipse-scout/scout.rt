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
 * Represents a {@link Runnable} to run a computation. Unlike {@link Runnable}, the run method is allowed to throw a
 * checked exception.
 *
 * @see Runnable
 * @see 5.1
 */
@FunctionalInterface
public interface IRunnable {

  /**
   * Computes a result, or throws an exception if unable to do so.
   */
  @SuppressWarnings("squid:S00112")
  void run() throws Exception;
}
