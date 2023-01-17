/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.security.AccessController;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * Translates exceptions origin from 'Java Executor Framework' into exceptions of Scout Job API.
 * <p>
 * Translation is not done in {@link IExceptionTranslator} because internal of Job API.
 *
 * @since 5.2
 */
@ApplicationScoped
public class JobExceptionTranslator {

  /**
   * Translates {@link CancellationException} into {@link ThreadInterruptedError}.
   */
  protected FutureCancelledError translateCancellationException(final CancellationException e, final String message) {
    return decorate(new FutureCancelledError(message, e));
  }

  /**
   * Translates {@link InterruptedException} into {@link ThreadInterruptedError}.
   */
  protected ThreadInterruptedError translateInterruptedException(final InterruptedException e, final String message) {
    return decorate(new ThreadInterruptedError(message, e));
  }

  /**
   * Translates {@link TimeoutException} into {@link TimedOutError}.
   */
  protected TimedOutError translateTimeoutException(final TimeoutException e, final String message, final long timeout, final TimeUnit unit) {
    return decorate(new TimedOutError(message, e).withContextInfo("timeout", "{}ms", unit.toMillis(timeout)));
  }

  /**
   * Translates {@link ExecutionException} into exception according to {@link IExceptionTranslator}.
   */
  protected <EXCEPTION extends Throwable> EXCEPTION translateExecutionException(final ExecutionException e, final Class<? extends IExceptionTranslator<EXCEPTION>> translator) {
    return decorate(BEANS.get(translator).translate(e)); // Do not unwrap ExecutionException here because to be done by translator, so that the submitter can also work with ExecutionException, e.g. by using NullExceptionTranslator.
  }

  /**
   * Method invoked to decorate an exception before given to the caller.
   */
  protected <EXCEPTION extends Throwable> EXCEPTION decorate(final EXCEPTION exception) {
    if (exception instanceof IThrowableWithContextInfo) {
      ((IThrowableWithContextInfo) exception)
          .withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())))
          .withContextInfo("calling-thread", Thread.currentThread().getName());
    }
    return exception;
  }
}
