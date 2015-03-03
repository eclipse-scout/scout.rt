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
package org.eclipse.scout.commons.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IExecutable;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Factory and utility methods to create executable objects.
 *
 * @since 5.1
 */
@Internal
public final class Executables {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Executables.class);

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
   * Returns a {@link Callable} that wraps the given {@link Callable} and associates it with a {@link IJobInput}.
   */
  public static <RESULT> CallableWithJobInput<RESULT> callableWithJobInput(final Callable<RESULT> callable, final IJobInput jobInput) {
    return new CallableWithJobInput<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return callable.call();
      }

      @Override
      public IJobInput getInput() {
        return jobInput;
      }
    };
  }

  /**
   * Returns a {@link Runnable} that wraps the given {@link Callable} and associates it with a {@link IJobInput}.
   */
  public static RunnableWithJobInput runnableWithJobInput(final Callable<Void> callable, final IJobInput jobInput) {
    return new RunnableWithJobInput() {

      @Override
      public void run() {
        try {
          callable.call();
        }
        catch (final Exception e) {
          LOG.error(String.format("Unhandled exception during job execution. [job=%s]", jobInput.getIdentifier()), e);
        }
      }

      @Override
      public IJobInput getInput() {
        return jobInput;
      }
    };
  }

  /**
   * {@link Callable} representing a task associated with a {@link IJobInput}.
   *
   * @since 5.1
   */
  public interface CallableWithJobInput<RESULT> extends Callable<RESULT> {

    /**
     * @return {@link IJobInput} associated with this {@link Callable}.
     */
    IJobInput getInput();
  }

  /**
   * {@link Runnable} representing a task associated with a {@link IJobInput}.
   *
   * @since 5.1
   */
  public interface RunnableWithJobInput extends Runnable {

    /**
     * @return {@link IJobInput} associated with this {@link Runnable}.
     */
    IJobInput getInput();
  }
}
