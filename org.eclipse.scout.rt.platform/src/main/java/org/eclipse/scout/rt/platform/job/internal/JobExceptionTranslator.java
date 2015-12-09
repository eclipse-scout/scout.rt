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

import java.security.AccessController;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;

/**
 * Translates exceptions origin from 'Java Executor Framework' into exceptions of Scout Job API.
 *
 * @since 5.2
 */
@ApplicationScoped
public class JobExceptionTranslator {

  /**
   * Translates {@link java.util.concurrent.CancellationException} into {@link InterruptedException}.
   */
  protected CancellationException translateCancellationException(final java.util.concurrent.CancellationException e, final String message) {
    return intercept(new CancellationException(message, e));
  }

  /**
   * Translates {@link java.lang.InterruptedException} into {@link InterruptedException}.
   */
  protected InterruptedException translateInterruptedException(final java.lang.InterruptedException e, final String message) {
    return intercept(new InterruptedException(message, e));
  }

  /**
   * Translates {@link java.util.concurrent.TimeoutException} into {@link TimeoutException}.
   */
  protected TimeoutException translateTimeoutException(final java.util.concurrent.TimeoutException e, final String message, final long timeout, final TimeUnit unit) {
    return intercept(new TimeoutException(message, e).withContextInfo("timeout", "{}ms", unit.toMillis(timeout)));
  }

  /**
   * Translates {@link ExecutionException} into exception according to {@link IThrowableTranslator}.
   */
  protected <ERROR extends Throwable> ERROR translateExecutionException(final ExecutionException e, final IThrowableTranslator<ERROR> translator) {
    return intercept(translator.translate(e.getCause()));
  }

  /**
   * Method invoked to intercept an exception before given to the caller.
   */
  protected <ERROR extends Throwable> ERROR intercept(final ERROR exception) {
    if (exception instanceof PlatformException) {
      ((PlatformException) exception)
          .withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())))
          .withContextInfo("thread", Thread.currentThread().getName());
    }
    return exception;
  }
}
