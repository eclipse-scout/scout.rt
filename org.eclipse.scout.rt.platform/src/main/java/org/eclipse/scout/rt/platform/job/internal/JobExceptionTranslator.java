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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;

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
   * Translates {@link java.util.concurrent.CancellationException} into {@link InterruptedException}.
   */
  protected CancellationException translateCancellationException(final java.util.concurrent.CancellationException e, final String message) {
    return decorate(new CancellationException(message, e));
  }

  /**
   * Translates {@link java.lang.InterruptedException} into {@link InterruptedException}.
   */
  protected InterruptedException translateInterruptedException(final java.lang.InterruptedException e, final String message) {
    return decorate(new InterruptedException(message, e));
  }

  /**
   * Translates {@link java.util.concurrent.TimeoutException} into {@link TimeoutException}.
   */
  protected TimeoutException translateTimeoutException(final java.util.concurrent.TimeoutException e, final String message, final long timeout, final TimeUnit unit) {
    return decorate(new TimeoutException(message, e).withContextInfo("timeout", "{}ms", unit.toMillis(timeout)));
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
    if (exception instanceof PlatformException) {
      ((PlatformException) exception)
          .withContextInfo("user", SecurityUtility.getPrincipalNames(Subject.getSubject(AccessController.getContext())))
          .withContextInfo("calling-thread", Thread.currentThread().getName());
    }
    return exception;
  }
}
