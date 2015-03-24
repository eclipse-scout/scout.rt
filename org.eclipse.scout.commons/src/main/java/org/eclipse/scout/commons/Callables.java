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

import org.eclipse.scout.commons.Assertions.AssertionException;
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
   * Returns a callable object that represents the given {@link IExecutable}.
   *
   * @throws AssertionException
   *           is thrown if the given {@link IExecutable} is not of the type {@link IRunnable} or {@link ICallable}.
   */
  public static <RESULT> ICallable<RESULT> callable(final IExecutable<RESULT> executable) {
    if (executable instanceof ICallable) {
      return (ICallable<RESULT>) executable;
    }
    else if (executable instanceof IRunnable) {
      return new ICallable<RESULT>() {

        @Override
        public RESULT call() throws Exception {
          ((IRunnable) executable).run();
          return null;
        }
      };
    }
    else {
      throw new AssertionException("Illegal executable provided: must be a '%s' or '%s'", IRunnable.class.getSimpleName(), ICallable.class.getSimpleName());
    }
  }

  /**
   * @return callable that does nothing when being called.
   */
  public static <RESULT> ICallable<Void> nullCallable() {
    return NULL_CALLABLE;
  }

  private static final ICallable<Void> NULL_CALLABLE = new ICallable<Void>() {

    @Override
    public Void call() throws Exception {
      return null; // NOOP
    }
  };
}
