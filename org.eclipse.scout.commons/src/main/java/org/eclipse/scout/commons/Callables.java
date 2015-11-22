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
package org.eclipse.scout.commons;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.annotations.Internal;

/**
 * Factory to create callable objects.
 *
 * @since 5.1
 */
@Internal
public final class Callables {

  private Callables() {
    // private constructor for utility classes.
  }

  /**
   * Returns a callable object that represents the given {@link IRunnable}.
   */
  public static Callable<Void> callable(final IRunnable runnable) {
    return new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        runnable.run();
        return null;
      }
    };
  }
}
