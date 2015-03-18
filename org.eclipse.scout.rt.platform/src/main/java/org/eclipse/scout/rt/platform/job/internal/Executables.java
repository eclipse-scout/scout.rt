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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.annotations.Internal;

/**
 * Factory to create executable objects.
 *
 * @since 5.1
 */
@Internal
public final class Executables {

  private Executables() {
    // private constructor for utility classes.
  }

  /**
   * Returns a {@link Callable} object representing the given {@link IExecutable}.
   *
   * @throws AssertionException
   *           is thrown if the given {@link IExecutable} is not of the type {@link IRunnable} or {@link ICallable}.
   */
  public static <RESULT> Callable<RESULT> callable(final IExecutable<RESULT> executable) {
    if (executable instanceof IRunnable) {
      return new Callable<RESULT>() {

        @Override
        public RESULT call() throws Exception {
          ((IRunnable) executable).run();
          return null;
        }
      };
    }
    else if (executable instanceof ICallable) {
      @SuppressWarnings("unchecked")
      final Callable<RESULT> callable = (Callable) executable;
      return callable;
    }
    else {
      throw new AssertionException("Illegal executable provided: must be a '%s' or '%s'", IRunnable.class.getSimpleName(), ICallable.class.getSimpleName());
    }
  }

  /**
   * @return {@link Callable} that does nothing when being called.
   */
  public static <RESULT> Callable<Void> nullCallable() {
    return NULL_CALLABLE;
  }

  private static final Callable<Void> NULL_CALLABLE = new Callable<Void>() {

    @Override
    public Void call() throws Exception {
      return null; // NOOP
    }
  };
}
