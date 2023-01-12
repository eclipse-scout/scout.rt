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

import java.util.concurrent.Callable;

/**
 * Factory to create callable objects.
 *
 * @since 5.1
 */
public final class Callables {

  private Callables() {
    // private constructor for utility classes.
  }

  /**
   * Returns a callable object that represents the given {@link IRunnable}.
   */
  public static Callable<Void> callable(final IRunnable runnable) {
    return () -> {
      runnable.run();
      return null;
    };
  }
}
