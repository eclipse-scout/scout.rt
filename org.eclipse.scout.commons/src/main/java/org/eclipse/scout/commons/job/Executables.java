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
package org.eclipse.scout.commons.job;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.job.internal.Future;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Factory and utility methods to create executable and future objects.
 *
 * @since 5.1
 */
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
   * Returns a {@link Callable} that wraps the given {@link Callable} and associates it with a {@link IJobInput}
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
   * Returns a {@link Runnable} object associated with a {@link IJobInput} that, when called, calls the given callable
   * and discards its result and exception.
   */
  public static RunnableWithJobInput runnableWithJobInput(final Callable<Void> callable, final IJobInput jobInput) {
    return new RunnableWithJobInput() {

      @Override
      public void run() {
        try {
          callable.call();
        }
        catch (final Exception e) {
          LOG.error(String.format("Unhandled exception during job execution. [job=%s]", jobInput.getIdentifier("n/a")), e);
        }
      }

      @Override
      public IJobInput getInput() {
        return jobInput;
      }
    };
  }

  /**
   * Returns a {@link IFuture} that wraps the given {@link Future}.
   *
   * @param future
   *          {@link Future} to be wrapped.
   * @param name
   *          name of the associated task used for logging purpose.
   * @return {@link IFuture}.
   */
  public static <RESULT> IFuture<RESULT> future(final java.util.concurrent.Future<RESULT> future, final String name) {
    return new Future<RESULT>(future, name);
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

  /**
   * Marker interface for an executable to be given to a job manager for execution.
   * <p/>
   * The job manager accepts one of the following implementing interfaces:
   * <ul>
   * <li>{@link IRunnable}: If executing a task that does not return a result to the caller.</li>
   * <li>{@link ICallable}: If executing a task that returns a result to the caller.</li>
   * </ul>
   *
   * @see IRunnable
   * @see ICallable
   * @since 5.1
   */
  public interface IExecutable<RESULT> {
  }
}
